package spreadsheet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.OptionalDouble;
import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.ast.ReferenceCollector;
import spreadsheet.formula.lexer.FormulaTokenizer;
import spreadsheet.formula.lexer.Token;
import spreadsheet.formula.parser.ShuntingYardParser;
import spreadsheet.formula.eval.CellLookup;


public class Spreadsheet implements CellLookup{
    private final Map<CellAddress, Cell> cells;
    private final Map<CellAddress, Set<CellAddress>> dependencies;
    private final Map<CellAddress, Set<CellAddress>> dependents;

    public Spreadsheet() {
        this.cells = new HashMap<>();
        this.dependencies = new HashMap<>();
        this.dependents = new HashMap<>();
    }

    // Public API
    public int getRowCount() { return getMaxRow(); }
    public int getColumnCount() { return getMaxColumn(); }

    public void setCellContent(CellAddress address, String content) {
        // Sets the content of a cell, updating dependencies and checking for cycles
        if (content == null || content.strip().isEmpty()) {
            removeCellAndEdges(address);
            refreshDependents(address);
            return;
        }

        Cell cell = cells.get(address);
        if (cell == null) {
            cell = new Cell(this, address, "");
            cells.put(address, cell);
        }

        Set<CellAddress> oldDeps = dependencies.getOrDefault(address, Set.of());
        Set<CellAddress> newDeps = collectDependencies(content);

        updateDependencies(address, oldDeps, newDeps);

        if (hasCycleFrom(address)) {
            updateDependencies(address, newDeps, oldDeps);
            throw new FormulaException("Circular reference found");
        }

        cell.setContent(content);
        refreshDependents(address);
    }

    public void setCellContent(String address, String content) {
        setCellContent(CellAddress.parseA1(address), content);
    }

    public void setCellContentRC(int rowIndex, int columnIndex, String content) {
        setCellContent(new CellAddress(rowIndex, columnIndex), content);
    }

    public String getCellContent(CellAddress address) {
        // Returns the raw content of the cell
        Cell cell = cells.get(address);
        if (cell == null) {
            return "";
        }
        return cell.getContent();
    }

    public String getCellContent(String address) {
        return getCellContent(CellAddress.parseA1(address));
    }

    public String getCellContentRC(int rowIndex, int columnIndex) {
        return getCellContent(new CellAddress(rowIndex, columnIndex));
    }

    public String getCellDisplayValue(CellAddress address) {
        // Returns the display value of the cell
        Cell cell = cells.get(address);
        if (cell == null) {
            return "";
        }
        return cell.getDisplayValue();
    }

    public String getCellDisplayValue(String address) {
        return getCellDisplayValue(CellAddress.parseA1(address));
    }

    public String getCellDisplayValueRC(int rowIndex, int columnIndex) {
        return getCellDisplayValue(new CellAddress(rowIndex, columnIndex));
    }

    public boolean isCellError(CellAddress address) {
        // for the ui
        Cell cell = cells.get(address);
        if (cell == null) {
            return false;
        }
        return "#ERR".equals(cell.getDisplayValue());
    }

    public boolean isCellError(String address) {
        return isCellError(CellAddress.parseA1(address));
    }

    public boolean isCellErrorRC(int rowIndex, int columnIndex) {
        return isCellError(new CellAddress(rowIndex, columnIndex));
    }

    public List<CellAddress> getNonEmptyCells() {
        // helper for the ui
        List<CellAddress> result = new ArrayList<>();
        for (Map.Entry<CellAddress, Cell> entry : cells.entrySet()) {
            String content = entry.getValue().getContent();
            if (content != null && !content.isEmpty()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public String getNonEmptyCellAddresses() {
        List<CellAddress> cells = getNonEmptyCells();
        StringBuilder builder = new StringBuilder();
        for (CellAddress address : cells) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(address.toA1());
        }
        return builder.toString();
    }

    public double findCell(int rowIndex, int columnIndex) {
        return resolveCellValue(rowIndex, columnIndex);
    }

    public OptionalDouble findCellOptional(int rowIndex, int columnIndex) {
        CellAddress target = new CellAddress(rowIndex, columnIndex);
        Cell targetCell = cells.get(target);
        if (targetCell == null) {
            return OptionalDouble.empty();
        }
        String content = targetCell.getContent();
        if (content == null || content.strip().isEmpty()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(targetCell.evaluateNumericValue());
    }

    // API / evaluation interface
    double resolveCellValue(int rowIndex, int columnIndex) {
        // Resolves the numeric value of a referenced cell
        CellAddress target = new CellAddress(rowIndex, columnIndex);
        Cell targetCell = cells.get(target);
        if (targetCell == null) {
            // Create a blank cell on first reference so empty cells behave as zero in numeric contexts.
            targetCell = new Cell(this, target, "");
            cells.put(target, targetCell);
        }
        return targetCell.evaluateNumericValue();
    }

    // Persistence
    public void loadFromFile(String filename) throws IOException {
        // load the spreadsheet from a s2v file
        resetState();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            loadFromReader(reader);
        }
    }

    public void loadFromS2vString(String s2v) {
        // load the spreadsheet from S2V content (semicolon-separated rows)
        // specifically to load the demo data in the github page since pages cant access files
        resetState();
        if (s2v == null || s2v.isEmpty()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new StringReader(s2v))) {
            loadFromReader(reader);
        } catch (IOException ex) {
            throw new IllegalStateException("Unexpected I/O while reading S2V data", ex);
        }
    }

    public void saveToFile(String filename) throws IOException {
        // save the spreadsheet to a CSV file
        int maxRow = getMaxRow();
        int maxCol = getMaxColumn();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int r = 1; r <= maxRow; r++) {
                StringBuilder line = new StringBuilder();

                for (int c = 1; c <= maxCol; c++) {
                    if (c > 1) {
                        line.append(';');
                    }

                    CellAddress address = new CellAddress(r, c);
                    String content = getCellContent(address);
                    if (content.startsWith("=")) {
                        line.append(content.replace(";", ","));
                    } else {
                        line.append(content);
                    }
                }

                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    private void resetState() {
        cells.clear();
        dependencies.clear();
        dependents.clear();
    }

    private void loadFromReader(BufferedReader reader) throws IOException {
        // Load the spreadsheet from a reader (used by loadFromFile and loadFromS2vString)
        String line;
        int rowIndex = 1;

        while ((line = reader.readLine()) != null) {
            List<String> tokens = splitS2vLine(line);

            for (int i = 0; i < tokens.size(); i++) {
                String content = tokens.get(i);
                int colIndex = i + 1;

                if (!content.isEmpty()) {
                    if (content.startsWith("=")) {
                        content = content.replace(",", ";");
                    }
                    CellAddress address = new CellAddress(rowIndex, colIndex);
                    setCellContent(address, content);
                }
            }
            rowIndex++;
        }
    }

    private List<String> splitS2vLine(String line) {
        // Split on semicolons, but ignore those inside function parentheses for loading edge case.
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '(') {
                parenDepth++;
            } else if (ch == ')' && parenDepth > 0) {
                parenDepth--;
            }

            if (ch == ';' && parenDepth == 0) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        tokens.add(current.toString());
        return tokens;
    }

    private int getMaxRow() {
        // helper for saveToFile to determine max row
        int max = 0;
        for (Map.Entry<CellAddress, Cell> entry : cells.entrySet()) {
            // because of the way empty referenced cells work
            // if you referred to a far out cell (eg D2000) and saved the file,
            // the spreadsheet would save all the empty cells before it
            // to fix this we only consider non-empty cells for max calculations
            String content = entry.getValue().getContent();
            if (content == null || content.isEmpty()) {
                continue;
            }
            int row = entry.getKey().getRow();
            if (row > max) {
                max = row;
            }
        }
        return max;
    }

    private int getMaxColumn() {
        // helper for saveToFile to determine max column
        int max = 0;
        for (Map.Entry<CellAddress, Cell> entry : cells.entrySet()) {
            String content = entry.getValue().getContent();
            if (content == null || content.isEmpty()) {
                continue;
            }
            int column = entry.getKey().getColumn();
            if (column > max) {
                max = column;
            }
        }
        return max;
    }

    // Dependency graph
    private Set<CellAddress> collectDependencies(String content) {
        // Collect cell references from the formula content
        if (content == null) {
            return Set.of();
        }
        String trimmed = content.stripLeading();
        if (!trimmed.startsWith("=")) {
            return Set.of();
        }
        String formula = trimmed.substring(1);
        List<Token> tokens = FormulaTokenizer.tokenize(formula);
        ExpressionNode ast = ShuntingYardParser.parse(tokens);
        return ReferenceCollector.collect(ast);
    }

    private void updateDependencies(CellAddress address,
                                    Set<CellAddress> oldDeps,
                                    Set<CellAddress> newDeps) {
        // Update the dependency graph when changing cell content
        for (CellAddress dep : oldDeps) {
            Set<CellAddress> reverse = dependents.get(dep);
            if (reverse != null) {
                reverse.remove(address);
                if (reverse.isEmpty()) {
                    dependents.remove(dep);
                }
            }
        }

        if (newDeps.isEmpty()) {
            dependencies.remove(address);
        } else {
            dependencies.put(address, new HashSet<>(newDeps));
        }

        for (CellAddress dep : newDeps) {
            dependents.computeIfAbsent(dep, key -> new HashSet<>()).add(address);
        }
    }

    private void removeCellAndEdges(CellAddress address) {
        // Added to remove the cell and its dependencies
        cells.remove(address);
        Set<CellAddress> oldDeps = dependencies.getOrDefault(address, Set.of());
        updateDependencies(address, oldDeps, Set.of());
    }

    private boolean hasCycleFrom(CellAddress start) {
        return dfsCycleCheck(start, new HashSet<>(), new HashSet<>());
    }

    private boolean dfsCycleCheck(CellAddress current,
                                  Set<CellAddress> visiting,
                                  Set<CellAddress> visited) {
        // Simple DFS to detect cycles
        if (visiting.contains(current)) {
            return true;
        }
        if (visited.contains(current)) {
            return false;
        }

        visiting.add(current);
        for (CellAddress neighbor : dependencies.getOrDefault(current, Set.of())) {
            if (dfsCycleCheck(neighbor, visiting, visited)) {
                return true;
            }
        }
        visiting.remove(current);
        visited.add(current);
        return false;
    }

    private void refreshDependents(CellAddress start) {
        // Simple BFS to refresh all dependent cells
        Deque<CellAddress> queue = new ArrayDeque<>();
        Set<CellAddress> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            CellAddress current = queue.removeFirst();
            Cell cell = cells.get(current);
            if (cell != null) {
                cell.recalculateDisplay();
            }
            for (CellAddress dependent : dependents.getOrDefault(current, Set.of())) {
                if (visited.add(dependent)) {
                    queue.add(dependent);
                }
            }
        }
    }
}

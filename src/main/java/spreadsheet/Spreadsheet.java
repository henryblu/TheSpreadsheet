package spreadsheet;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException; 

import spreadsheet.exceptions.FormulaException;

public class Spreadsheet {
    private Map<CellAddress, Cell> cells;
    
    public Spreadsheet(){ this.cells = new HashMap<>(); }
    public int getRowCount(){ return getMaxRow(); }
    public int getColumnCount(){ return getMaxColumn(); }

    public void setCellContent(CellAddress address, String content){
        if (content == null){
            cells.remove(address);
            return;
        } 

        Cell cell = cells.get(address);

        if (cell == null){
            cell = new Cell(this, address,content);
            cells.put(address,cell);
        }
        cell.setContent(content);

    }

    public String getCellContent(CellAddress address){
        Cell cell = cells.get(address);
        if (cell == null){ 
            return ""; 
        }
        return cell.getContent();
    }

    double resolveCellValue(int rowIndex, int columnIndex) {
        CellAddress target = new CellAddress(rowIndex, columnIndex);
        Cell targetCell = cells.get(target);
        if (targetCell == null) {
            throw new FormulaException("Referenced cell '" + columnLabel(columnIndex) + rowIndex + "' is empty");
        }
        return targetCell.evaluateNumericValue();
    }

    private static String columnLabel(int column) {
        StringBuilder builder = new StringBuilder();
        int current = column;
        while (current > 0) {
            int remainder = (current - 1) % 26;
            builder.insert(0, (char) ('A' + remainder));
            current = (current - 1) / 26;
        }
        return builder.toString();
    }

    public String getCellDisplayValue(CellAddress address) {
        Cell cell = cells.get(address);
        if (cell == null) {
            return "";
        }
        return cell.getDisplayValue();
    }

    public void loadFromFile(String filename) throws IOException {
        cells.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int rowIndex = 1;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";", -1);

                for (int i = 0; i < tokens.length; i++) {
                    String content = tokens[i];
                    int colIndex = i + 1;

                    if (!content.isEmpty()) {
                        CellAddress address = new CellAddress(rowIndex, colIndex);
                        setCellContent(address, content);
                    }
                }
                rowIndex++;
            }
        }
    }


    private int getMaxRow() {
        int max = 0;
        for (CellAddress addr : cells.keySet()) {
            if (addr.getRow() > max) {
                max = addr.getRow();
            }
        }
        return max;
    }

    private int getMaxColumn() {
        int max = 0;
        for (CellAddress addr : cells.keySet()) {
            if (addr.getColumn() > max) {
                max = addr.getColumn();
            }
        }
        return max;
    }

    public void saveToFile(String filename) throws IOException {
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
                    line.append(content);
                }

                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

}

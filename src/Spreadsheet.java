import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException; 

public class Spreadsheet {
    private Map<CellAddress, Cell> cells;
    private int maxRow = 0;
    private int maxColumn = 0;

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
            cell = new Cell(address,content);
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

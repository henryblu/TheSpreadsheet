package spreadsheet;
public class CellAddress {
    private int row;
    private int column;

    public CellAddress(int row, int column){
        this.row = row;
        this.column = column;
    }
    public int getRow(){return row;}
    public int getColumn(){return column;}

    public String toA1() {
        return columnLabel(column) + row;
    }

    public static CellAddress parseA1(String address) {
        // added this from the web interface to avoid dependency on regex
        if (address == null) {
            throw new IllegalArgumentException("Cell address is required");
        }
        String trimmed = address.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Cell address is required");
        }

        int i = 0;
        int column = 0;
        while (i < trimmed.length() && Character.isLetter(trimmed.charAt(i))) {
            char ch = Character.toUpperCase(trimmed.charAt(i));
            column = (column * 26) + (ch - 'A' + 1);
            i++;
        }
        if (column == 0) {
            throw new IllegalArgumentException("Column is missing in address: " + address);
        }

        int rowStart = i;
        while (i < trimmed.length() && Character.isDigit(trimmed.charAt(i))) {
            i++;
        }
        if (rowStart == i || i != trimmed.length()) {
            throw new IllegalArgumentException("Row is missing or invalid in address: " + address);
        }
        int row = Integer.parseInt(trimmed.substring(rowStart));
        if (row <= 0) {
            throw new IllegalArgumentException("Row must be positive in address: " + address);
        }

        return new CellAddress(row, column);
    }

    public static String columnLabel(int column) {
        StringBuilder builder = new StringBuilder();
        int current = column;
        while (current > 0) {
            int remainder = (current - 1) % 26;
            builder.insert(0, (char) ('A' + remainder));
            current = (current - 1) / 26;
        }
        return builder.toString();
    }

    public int hashCode(){
        return 31* row + column;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (!(obj instanceof CellAddress)) return false; 
        
        CellAddress other = (CellAddress) obj;
        return this.row == other.row && this.column == other.column;
    }
}

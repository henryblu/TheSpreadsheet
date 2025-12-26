package spreadsheet.web;

import java.util.List;
import org.teavm.jso.JSExport;
import spreadsheet.CellAddress;
import spreadsheet.Spreadsheet;

public final class DemoEntry {
    private static Spreadsheet spreadsheet = new Spreadsheet();

    private DemoEntry() {
    }

    public static void main(String[] args) {
        // TeaVM needs a main entrypoint, even when using JS exports.
    }

    @JSExport
    public static void reset() {
        spreadsheet = new Spreadsheet();
    }

    @JSExport
    public static void loadFromS2v(String s2v) {

        spreadsheet.loadFromS2vString(s2v);
    }

    @JSExport
    public static void setCellContent(String address, String content) {
        spreadsheet.setCellContent(parseAddress(address), content);
    }

    @JSExport
    public static String getCellContent(String address) {
        return spreadsheet.getCellContent(parseAddress(address));
    }

    @JSExport
    public static String getCellDisplay(String address) {
        return spreadsheet.getCellDisplayValue(parseAddress(address));
    }

    @JSExport
    public static void setCellContentRC(int row, int column, String content) {
        spreadsheet.setCellContent(new CellAddress(row, column), content);
    }

    @JSExport
    public static String getCellContentRC(int row, int column) {
        return spreadsheet.getCellContent(new CellAddress(row, column));
    }

    @JSExport
    public static String getCellDisplayRC(int row, int column) {
        return spreadsheet.getCellDisplayValue(new CellAddress(row, column));
    }

    @JSExport
    public static boolean isCellError(String address) {
        return spreadsheet.isCellError(parseAddress(address));
    }

    @JSExport
    public static boolean isCellErrorRC(int row, int column) {
        return spreadsheet.isCellError(new CellAddress(row, column));
    }

    @JSExport
    public static String getNonEmptyCellAddresses() {
        List<CellAddress> cells = spreadsheet.getNonEmptyCells();
        StringBuilder builder = new StringBuilder();
        for (CellAddress address : cells) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(toA1(address));
        }
        return builder.toString();
    }

    @JSExport
    public static int getRowCount() {
        return spreadsheet.getRowCount();
    }

    @JSExport
    public static int getColumnCount() {
        return spreadsheet.getColumnCount();
    }

    private static CellAddress parseAddress(String address) {
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

    private static String toA1(CellAddress address) {
        return columnLabel(address.getColumn()) + address.getRow();
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
}

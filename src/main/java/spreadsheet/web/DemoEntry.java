package spreadsheet.web;

import org.teavm.jso.JSExport;
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
        spreadsheet.setCellContent(address, content);
    }

    @JSExport
    public static String getCellContent(String address) {
        return spreadsheet.getCellContent(address);
    }

    @JSExport
    public static String getCellDisplay(String address) {
        return spreadsheet.getCellDisplayValue(address);
    }

    @JSExport
    public static void setCellContentRC(int row, int column, String content) {
        spreadsheet.setCellContentRC(row, column, content);
    }

    @JSExport
    public static String getCellContentRC(int row, int column) {
        return spreadsheet.getCellContentRC(row, column);
    }

    @JSExport
    public static String getCellDisplayRC(int row, int column) {
        return spreadsheet.getCellDisplayValueRC(row, column);
    }

    @JSExport
    public static boolean isCellError(String address) {
        return spreadsheet.isCellError(address);
    }

    @JSExport
    public static boolean isCellErrorRC(int row, int column) {
        return spreadsheet.isCellErrorRC(row, column);
    }

    @JSExport
    public static String getNonEmptyCellAddresses() {
        return spreadsheet.getNonEmptyCellAddresses();
    }

    @JSExport
    public static int getRowCount() {
        return spreadsheet.getRowCount();
    }

    @JSExport
    public static int getColumnCount() {
        return spreadsheet.getColumnCount();
    }

}

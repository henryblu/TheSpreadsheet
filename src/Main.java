import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Spreadsheet sheet = new Spreadsheet();

        sheet.setCellContent(new CellAddress(1, 1), "hello");
        sheet.setCellContent(new CellAddress(1, 2), "world");
        sheet.setCellContent(new CellAddress(1, 3), "123");
        sheet.setCellContent(new CellAddress(2, 1), "test");

        sheet.saveToFile("../data/sample.s2v");

        Spreadsheet loaded = new Spreadsheet();
        loaded.loadFromFile("../data/sample.s2v");

        System.out.println(loaded.getCellContent(new CellAddress(1,1)));
        System.out.println(loaded.getCellContent(new CellAddress(1,2)));
        System.out.println(loaded.getCellContent(new CellAddress(1,3)));
        System.out.println(loaded.getCellContent(new CellAddress(2,1)));
    }

}

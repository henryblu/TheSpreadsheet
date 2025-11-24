package spreadsheet;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // Write and read a sample file in the project-level data directory.
        String samplePath = "data/sample.s2v";
        new File("data").mkdirs();

        Spreadsheet sheet = new Spreadsheet();

        sheet.setCellContent(new CellAddress(1, 1), "hello");
        sheet.setCellContent(new CellAddress(1, 2), "world");
        sheet.setCellContent(new CellAddress(1, 3), "123");
        sheet.setCellContent(new CellAddress(2, 1), "test");
        sheet.setCellContent(new CellAddress(2, 2), " =1+2*3/6  ");

        sheet.saveToFile(samplePath);

        Spreadsheet loaded = new Spreadsheet();
        loaded.loadFromFile(samplePath);

        System.out.println(loaded.getCellContent(new CellAddress(1,1)));
        System.out.println(loaded.getCellContent(new CellAddress(1,2)));
        System.out.println(loaded.getCellContent(new CellAddress(1,3)));
        System.out.println(loaded.getCellContent(new CellAddress(2,1)));
        System.out.println("Formula preview: " + loaded.getCellDisplayValue(new CellAddress(2,2)));
    }

}

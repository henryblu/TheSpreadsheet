package spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import spreadsheet.exceptions.FormulaException;

class SpreadsheetTest {

    @TempDir
    Path tempDir;

    @Test
    void emptyCellsReturnEmptyString() {
        Spreadsheet sheet = new Spreadsheet();

        assertEquals("", sheet.getCellContent(new CellAddress(3, 5)));
        assertEquals(0, sheet.getRowCount());
        assertEquals(0, sheet.getColumnCount());
    }

    @Test
    void setCellContentStoresValue() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);

        sheet.setCellContent(a1, "42");

        assertEquals("42", sheet.getCellContent(a1));
        assertEquals(1, sheet.getRowCount(), "Row count grows with populated cells");
        assertEquals(1, sheet.getColumnCount(), "Column count grows with populated cells");

        sheet.setCellContent(a1, null);
        assertEquals("", sheet.getCellContent(a1), "Null content removes the cell");
        assertEquals(0, sheet.getRowCount());
        assertEquals(0, sheet.getColumnCount());
    }

    @Test
    void formulasAreEvaluatedForDisplay() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);

        sheet.setCellContent(a1, "=1+2*3");

        assertEquals("=1+2*3", sheet.getCellContent(a1));
        assertEquals("7.0", sheet.getCellDisplayValue(a1));
    }

    @Test
    void invalidFormulaShowsError() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);

        sheet.setCellContent(a1, "=1/0");

        assertEquals("#ERR", sheet.getCellDisplayValue(a1));
    }

    @Test
    void cellReferenceUsesTargetValue() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(a1, "42");
        sheet.setCellContent(b1, "=A1");

        assertEquals("=A1", sheet.getCellContent(b1));
        assertEquals("42.0", sheet.getCellDisplayValue(b1));
    }

    @Test
    void referenceToFormulaCellUsesEvaluatedValue() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(a1, "=2+3");
        sheet.setCellContent(b1, "=A1*2");

        assertEquals("10.0", sheet.getCellDisplayValue(b1));
    }

    @Test
    void referencingEmptyCellShowsError() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(b1, "=A1");

        assertEquals("#ERR", sheet.getCellDisplayValue(b1));
        sheet.setCellContent(a1, "5");
        sheet.setCellContent(b1, "=A1");
        assertEquals("5.0", sheet.getCellDisplayValue(b1));
    }

    @Test
    void saveAndLoadWorks() throws IOException {
        Spreadsheet sheet = new Spreadsheet();
        sheet.setCellContent(new CellAddress(1, 1), "Hello");
        sheet.setCellContent(new CellAddress(2, 2), "42");

        Path file = tempDir.resolve("sheet.csv");
        sheet.saveToFile(file.toString());

        Spreadsheet loaded = new Spreadsheet();
        loaded.loadFromFile(file.toString());

        assertEquals("Hello", loaded.getCellContent(new CellAddress(1, 1)));
        assertEquals("42", loaded.getCellContent(new CellAddress(2, 2)));
    }

    @Test
    void dependentCellRefreshesWhenReferenceChanges() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(a1, "5");
        sheet.setCellContent(b1, "=A1");
        assertEquals("5.0", sheet.getCellDisplayValue(b1));

        sheet.setCellContent(a1, "6");
        assertEquals("6.0", sheet.getCellDisplayValue(b1));
    }

    @Test
    void circularReferenceIsRejected() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(a1, "=B1");

        assertThrows(FormulaException.class, () -> sheet.setCellContent(b1, "=A1"));
    }
}

package spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    @Test
    void sumSupportsNumericArguments() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);

        sheet.setCellContent(a1, "=SUM(1;2;3)");

        assertEquals("6.0", sheet.getCellDisplayValue(a1));
    }

    @Test
    void minAndMaxWorkOverRanges() {
        Spreadsheet sheet = new Spreadsheet();
        sheet.setCellContent(new CellAddress(1, 1), "5");  // A1
        sheet.setCellContent(new CellAddress(2, 1), "2");  // A2
        sheet.setCellContent(new CellAddress(1, 2), "8");  // B1
        sheet.setCellContent(new CellAddress(2, 2), "4");  // B2

        sheet.setCellContent(new CellAddress(1, 3), "=MIN(A1:B2)");
        sheet.setCellContent(new CellAddress(2, 3), "=MAX(A1:B2)");

        assertEquals("2.0", sheet.getCellDisplayValue(new CellAddress(1, 3)));
        assertEquals("8.0", sheet.getCellDisplayValue(new CellAddress(2, 3)));
    }

    @Test
    void averageSupportsMixedArguments() {
        Spreadsheet sheet = new Spreadsheet();
        sheet.setCellContent(new CellAddress(1, 1), "2");  // A1
        sheet.setCellContent(new CellAddress(2, 1), "4");  // A2
        sheet.setCellContent(new CellAddress(1, 2), "6");  // B1

        sheet.setCellContent(new CellAddress(1, 3), "=AVERAGE(A1:A2;B1;8)");

        assertEquals("5.0", sheet.getCellDisplayValue(new CellAddress(1, 3)));
    }

    @Test
    // testing the example from the assignment description =1 + A1*((SUM(A2:B5;AVERAGE(B6:D8);C1;27)/4)+(D6-D8))

    void complexFormulaFromDescriptionIsEvaluatedCorrectly() {
        Spreadsheet sheet = new Spreadsheet();
        sheet.setCellContent(new CellAddress(1, 1), "3");   // A1
        sheet.setCellContent(new CellAddress(2, 1), "4");   // A2
        sheet.setCellContent(new CellAddress(2, 2), "6");   // B2
        sheet.setCellContent(new CellAddress(3, 3), "8");   // C3
        sheet.setCellContent(new CellAddress(6, 4), "10");  // D6
        sheet.setCellContent(new CellAddress(8, 4), "2");   // D8

        sheet.setCellContent(new CellAddress(1, 5), "=1 + A1*((SUM(A2:B5;AVERAGE(B6:D8);C3;28)/4)+(D6-D8))");
        // resolves to 1 + 3 * (( (4+6+(10+2)/2 +8 +28)/4) + (10-2)) = 64
        assertEquals("64.0", sheet.getCellDisplayValue(new CellAddress(1, 5)));
    }
    @Test
    void nestedFunctionsCanBeCombined() {
        Spreadsheet sheet = new Spreadsheet();
        sheet.setCellContent(new CellAddress(1, 1), "1");  // A1
        sheet.setCellContent(new CellAddress(2, 1), "2");  // A2
        sheet.setCellContent(new CellAddress(1, 2), "3");  // B1
        sheet.setCellContent(new CellAddress(2, 2), "4");  // B2

        sheet.setCellContent(new CellAddress(1, 3), "=SUM(A1:B2;AVERAGE(A1:B2))");

        assertEquals("12.5", sheet.getCellDisplayValue(new CellAddress(1, 3)));
    }

    @Test
    void numericStringsCanBeUsedAsNumbersInFormulas() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(a1, "1.5");
        sheet.setCellContent(b1, "=A1+1");

        assertEquals("2.5", sheet.getCellDisplayValue(b1));
    }

    @Test
    void emptyTextShouldBehaveAsZeroInNumericContexts() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(a1, "");
        sheet.setCellContent(b1, "=A1+1");

        assertEquals("1.0", sheet.getCellDisplayValue(b1));
    }

    @Test
    void nonNumericTextInFormulaCausesError() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(a1, "Hello");
        sheet.setCellContent(b1, "=A1");

        assertEquals("#ERR", sheet.getCellDisplayValue(b1));
    }

    @Test
    void multiLetterColumnReferencesAreSupported() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress aa1 = new CellAddress(1, 27);
        CellAddress b1 = new CellAddress(1, 2);

        sheet.setCellContent(aa1, "7");
        sheet.setCellContent(b1, "=AA1+1");

        assertEquals("8.0", sheet.getCellDisplayValue(b1));
    }

    @Test
    void rangesAllowReversedCorners() {
        Spreadsheet sheet = new Spreadsheet();
        sheet.setCellContent(new CellAddress(1, 1), "1");  // A1
        sheet.setCellContent(new CellAddress(2, 2), "4");  // B2

        sheet.setCellContent(new CellAddress(1, 3), "=SUM(B2:A1)");

        assertEquals("5.0", sheet.getCellDisplayValue(new CellAddress(1, 3)));
    }

    @Test
    void rangeOutsideFunctionIsRejected() {
        Spreadsheet sheet = new Spreadsheet();

        assertThrows(FormulaException.class,
                () -> sheet.setCellContent(new CellAddress(1, 1), "=A1:B2"));
    }

    @Test
    void malformedFormulaIsRejectedOnSet() {
        Spreadsheet sheet = new Spreadsheet();

        assertThrows(FormulaException.class,
                () -> sheet.setCellContent(new CellAddress(1, 1), "=1+"));
    }

    @Test
    void indirectCircularReferenceIsRejected() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);
        CellAddress c1 = new CellAddress(1, 3);

        sheet.setCellContent(a1, "=B1");
        sheet.setCellContent(b1, "=C1");

        assertThrows(FormulaException.class, () -> sheet.setCellContent(c1, "=A1"));
    }

    @Test
    void chainedDependentsRefreshWhenRootChanges() {
        Spreadsheet sheet = new Spreadsheet();
        CellAddress a1 = new CellAddress(1, 1);
        CellAddress b1 = new CellAddress(1, 2);
        CellAddress c1 = new CellAddress(1, 3);

        sheet.setCellContent(a1, "1");
        sheet.setCellContent(b1, "=A1+1");
        sheet.setCellContent(c1, "=B1+1");

        assertEquals("3.0", sheet.getCellDisplayValue(c1));

        sheet.setCellContent(a1, "4");
        assertEquals("6.0", sheet.getCellDisplayValue(c1));
    }

    @Test
    void saveUsesCommaInsideFunctionArguments() throws IOException {
        Spreadsheet sheet = new Spreadsheet();
        sheet.setCellContent(new CellAddress(1, 1), "=SUM(1;2;3)");

        Path file = tempDir.resolve("functions.csv");
        sheet.saveToFile(file.toString());

        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        assertEquals("=SUM(1,2,3)", lines.get(0));
    }

    @Test
    void loadConvertsCommaBackToSemicolonInFunctions() throws IOException {
        Path file = tempDir.resolve("load-functions.csv");
        Files.writeString(file, "=SUM(1,2,3)\n");

        Spreadsheet sheet = new Spreadsheet();
        sheet.loadFromFile(file.toString());

        assertEquals("=SUM(1;2;3)", sheet.getCellContent(new CellAddress(1, 1)));
        assertEquals("6.0", sheet.getCellDisplayValue(new CellAddress(1, 1)));
    }

    @Test
    void formulasAreEvaluatedAfterLoading() throws IOException {
        Path file = tempDir.resolve("load-formula.csv");
        Files.writeString(file, "2;=A1+3\n");

        Spreadsheet sheet = new Spreadsheet();
        sheet.loadFromFile(file.toString());

        assertEquals("5.0", sheet.getCellDisplayValue(new CellAddress(1, 2)));
    }
}

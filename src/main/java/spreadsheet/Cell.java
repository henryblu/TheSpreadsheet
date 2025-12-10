package spreadsheet;
import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.FormulaEngine;
public class Cell {
    private final CellAddress address;
    private String content;
    private String displayValue;

    public Cell(CellAddress address, String content) {
        this.address = address;
        setContent(content);
    }

    public CellAddress getAddress() { return address; }

    public String getContent() { return content; }

    public String getDisplayValue() { return displayValue; }

    public void setContent(String content) {
        this.content = (content == null) ? "" : content;
        evaluate();
    }

    private void evaluate() {
        String trimmed = content.stripLeading();
        if (!trimmed.startsWith("=")) {
            displayValue = content;
            return;
        }

        String formula = trimmed.substring(1);
        try {
            double result = FormulaEngine.evaluate(formula);
            displayValue = Double.toString(result);
        } catch (FormulaException ex) {
            displayValue = "#ERR";
        }
    }
}

package spreadsheet;
import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.FormulaEngine;

public class Cell {
    private final Spreadsheet spreadsheet;
    private final CellAddress address;
    private String content;
    private String displayValue;

    public Cell(Spreadsheet spreadsheet, CellAddress address, String content) {
        this.spreadsheet = spreadsheet;
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

    void recalculateDisplay() {
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
            double result = FormulaEngine.evaluate(formula, spreadsheet);
            displayValue = Double.toString(result);
        } catch (FormulaException ex) {
            displayValue = "#ERR";
        }
    }

    double evaluateNumericValue() {
        String Candidate = content.trim();
        String trimmed = content.stripLeading();

        if (!trimmed.startsWith("=")) {
            return parseLiteral(Candidate);
        }

        String formula = trimmed.substring(1);
        return FormulaEngine.evaluate(formula, spreadsheet);
    }
 
    private double parseLiteral(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw new FormulaException("Referenced cell '" + addressString() + "' is not numeric", ex);
        }
    }

    private String addressString() {
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

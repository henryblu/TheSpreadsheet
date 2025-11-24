import java.util.List;

import exceptions.FormulaException;
import formula.FormulaEvaluator;
import formula.FormulaTokenizer;
import formula.Token;
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
            List<Token> tokens = FormulaTokenizer.tokenize(formula);
            double result = FormulaEvaluator.evaluate(tokens);
            displayValue = Double.toString(result);
        } catch (FormulaException ex) {
            displayValue = "#ERR";
        }
    }
}

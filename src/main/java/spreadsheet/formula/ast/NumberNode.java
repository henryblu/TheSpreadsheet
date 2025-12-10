package spreadsheet.formula.ast;

public class NumberNode implements ExpressionNode {
    private final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}

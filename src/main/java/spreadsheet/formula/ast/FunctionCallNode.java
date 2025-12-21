package spreadsheet.formula.ast;

import java.util.List;

public class FunctionCallNode implements ExpressionNode {
    private final String name;
    private final List<ExpressionNode> args;

    public FunctionCallNode(String name, List<ExpressionNode> args) {
        this.name = name;
        this.args = args;
    }
    public String getName() { return name; }
    public List<ExpressionNode> getArgs() { return args; }
}
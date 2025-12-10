package spreadsheet.formula.ast;

import spreadsheet.formula.lexer.TokenType;

public class BinaryOpNode implements ExpressionNode {
    private final TokenType operator;
    private final ExpressionNode left;
    private final ExpressionNode right;

    public BinaryOpNode(TokenType operator, ExpressionNode left, ExpressionNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public TokenType getOperator() {
        return operator;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }
}

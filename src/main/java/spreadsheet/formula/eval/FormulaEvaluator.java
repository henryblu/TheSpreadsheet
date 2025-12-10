package spreadsheet.formula.eval;

import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.ast.BinaryOpNode;
import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.ast.NumberNode;
import spreadsheet.formula.lexer.TokenType;

public final class FormulaEvaluator {
    private FormulaEvaluator() {
    }

    public static double evaluate(ExpressionNode node) {
        return evaluateNode(node);
    }

    private static double evaluateNode(ExpressionNode node) {
        if (node instanceof NumberNode) {
            return ((NumberNode) node).getValue();
        }
        if (node instanceof BinaryOpNode) {
            BinaryOpNode opNode = (BinaryOpNode) node;
            double left = evaluateNode(opNode.getLeft());
            double right = evaluateNode(opNode.getRight());
            return applyOperator(opNode.getOperator(), left, right);
        }
        throw new FormulaException("Unknown expression node: " + node.getClass().getSimpleName());
    }

    private static double applyOperator(TokenType op, double left, double right) {
        switch (op) {
            case PLUS:
                return left + right;
            case MINUS:
                return left - right;
            case MULTIPLY:
                return left * right;
            case DIVIDE:
                if (right == 0.0) {
                    throw new FormulaException("Division by zero");
                }
                return left / right;
            default:
                throw new FormulaException("Unknown operator: " + op);
        }
    }
}

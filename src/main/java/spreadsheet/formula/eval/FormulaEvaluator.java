package spreadsheet.formula.eval;

import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.ast.BinaryOpNode;
import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.ast.NumberNode;
import spreadsheet.formula.ast.ReferenceNode;
import spreadsheet.formula.lexer.TokenType;

public final class FormulaEvaluator {
    private FormulaEvaluator() {
    }

    public static double evaluate(ExpressionNode node) {
        return evaluate(node, null);
    }

    public static double evaluate(ExpressionNode node, CellLookup lookup) {
        return evaluateNode(node, lookup);
    }

    private static double evaluateNode(ExpressionNode node, CellLookup lookup) {
        if (node instanceof NumberNode) {
            return ((NumberNode) node).getValue();
        }
        if (node instanceof ReferenceNode) {
            ReferenceNode refNode = (ReferenceNode) node;
            return lookup.findCell(refNode.getRowIndex(), refNode.getColumnIndex());
        }
        if (node instanceof BinaryOpNode) {
            BinaryOpNode opNode = (BinaryOpNode) node;
            double left = evaluateNode(opNode.getLeft(), lookup);
            double right = evaluateNode(opNode.getRight(), lookup);
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

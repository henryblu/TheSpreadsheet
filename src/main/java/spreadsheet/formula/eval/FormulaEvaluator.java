package spreadsheet.formula.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.ast.BinaryOpNode;
import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.ast.FunctionCallNode;
import spreadsheet.formula.ast.NumberNode;
import spreadsheet.formula.ast.RangeBounds;
import spreadsheet.formula.ast.RangeNode;
import spreadsheet.formula.ast.ReferenceNode;
import spreadsheet.formula.functions.FunctionEvaluator;
import spreadsheet.formula.functions.FunctionType;
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
            if (lookup == null) {
                throw new FormulaException("Cell lookup not provided");
            }
            ReferenceNode refNode = (ReferenceNode) node;
            return lookup.findCell(refNode.getRowIndex(), refNode.getColumnIndex());
        }
        if (node instanceof RangeNode) {
            throw new FormulaException("Range only allowed inside function");
        }
        if (node instanceof FunctionCallNode) {
            return evaluateFunction((FunctionCallNode) node, lookup);
        }
        if (node instanceof BinaryOpNode) {
            BinaryOpNode opNode = (BinaryOpNode) node;
            double left = evaluateNode(opNode.getLeft(), lookup);
            double right = evaluateNode(opNode.getRight(), lookup);
            return applyOperator(opNode.getOperator(), left, right);
        }
        throw new FormulaException("Unknown expression node: " + node.getClass().getSimpleName());
    }

    private static double evaluateFunction(FunctionCallNode node, CellLookup lookup) {
        FunctionType type = FunctionType.fromName(node.getName());
        List<Double> values = new ArrayList<>();
        for (ExpressionNode arg : node.getArgs()) {
            collectValues(arg, values, lookup);
        }
        try {
            return FunctionEvaluator.evaluate(type, values);
        } catch (IllegalArgumentException ex) {
            throw new FormulaException(ex.getMessage(), ex);
        }
    }

    private static void collectValues(ExpressionNode arg,
                                      List<Double> values,
                                      CellLookup lookup) {
        if (arg instanceof RangeNode) {
            addRangeValues((RangeNode) arg, values, lookup);
            return;
        }
        values.add(evaluateNode(arg, lookup));
    }

    private static void addRangeValues(RangeNode range,
                                       List<Double> values,
                                       CellLookup lookup) {
        if (lookup == null) {
            throw new FormulaException("Cell lookup not provided");
        }
        RangeBounds bounds = range.toBounds();
        for (int r = bounds.getRowMin(); r <= bounds.getRowMax(); r++) {
            for (int c = bounds.getColMin(); c <= bounds.getColMax(); c++) {
                OptionalDouble value = lookup.findCellOptional(r, c);
                if (value.isPresent()) {
                    values.add(value.getAsDouble());
                }
            }
        }
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

package spreadsheet.formula;

import java.util.List;
import java.util.Stack;

import spreadsheet.exceptions.FormulaException;

public class FormulaEvaluator {
    public static double evaluate(List<Token> tokens){
        Stack<Double> values = new Stack<>();
        Stack<TokenType> operators = new Stack<>();
        for (Token token : tokens){
            TokenType type = token.getType();

            if (type == TokenType.NUMBER) {
                values.push(token.getValue());
            } 
            else if (type == TokenType.LPAREN){
                operators.push(type);
            } 
            else if (type == TokenType.RPAREN) {
                collapseUntilLeftParen(values, operators);
            } 
            else if (isOperator(type)) {
                collapseByPrecedence(type, values, operators);
                operators.push(type);
            }
            else if (type == TokenType.EOF) {
                break;
            } 
            else {
                throw new FormulaException("Unknown token: " + type);
            }
        }
        while (!operators.isEmpty()) {
            if (operators.peek() == TokenType.LPAREN) {
                throw new FormulaException("Missing ')'");
            }
            applyTopOperator(values, operators);
        }

        if (values.size() != 1) {
            throw new FormulaException("Invalid expression");
        }
        return values.pop();
    }

    private static void collapseUntilLeftParen(Stack<Double> values, Stack<TokenType> operators) {
        while (!operators.isEmpty() && operators.peek() != TokenType.LPAREN) {
            applyTopOperator(values, operators);
        }
        if (operators.isEmpty()) {
            throw new FormulaException("Missing '('");
        }
        operators.pop();
    }

    private static void collapseByPrecedence(TokenType incoming,
                                            Stack<Double> values,
                                            Stack<TokenType> operators) {
        while (!operators.isEmpty()
                && isOperator(operators.peek())
                && precedence(operators.peek()) >= precedence(incoming)) {
            applyTopOperator(values, operators);
        }
    }

    private static boolean isOperator(TokenType type) {
        return type == TokenType.PLUS
            || type == TokenType.MINUS
            || type == TokenType.MULTIPLY
            || type == TokenType.DIVIDE;
    }

    private static int precedence(TokenType type) {
        if (type == TokenType.MULTIPLY || type == TokenType.DIVIDE) {
            return 2;
        }
        if (type == TokenType.PLUS || type == TokenType.MINUS) {
            return 1;
        }
        return 0;
    }

    private static void applyTopOperator(Stack<Double> values, Stack<TokenType> operators) {
        if (values.size() < 2) {
            throw new FormulaException("Missing operand");
        }
        double right = values.pop();
        double left = values.pop();
        TokenType op = operators.pop();

        double result;
        switch (op) {
            case PLUS:
                result = left + right;
                break;
            case MINUS:
                result = left - right;
                break;
            case MULTIPLY:
                result = left * right;
                break;
            case DIVIDE:
                if (right == 0.0) {
                    throw new FormulaException("Division by zero");
                }
                result = left / right;
                break;
            default:
                throw new FormulaException("Unknown operator: " + op);
        }

        values.push(result);
    }
}

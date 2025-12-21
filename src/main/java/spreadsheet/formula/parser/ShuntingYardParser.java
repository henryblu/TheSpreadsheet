package spreadsheet.formula.parser;

import java.util.List;
import java.util.Stack;

import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.ast.BinaryOpNode;
import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.ast.NumberNode;
import spreadsheet.formula.lexer.Token;
import spreadsheet.formula.lexer.TokenType;
import spreadsheet.formula.ast.ReferenceNode;

public final class ShuntingYardParser {
    private ShuntingYardParser() {
    }

    public static ExpressionNode parse(List<Token> tokens) {
        Stack<TokenType> operators = new Stack<>();
        Stack<ExpressionNode> operands = new Stack<>();

        for (Token token : tokens) {
            TokenType type = token.getType();

            if (type == TokenType.NUMBER) {
                operands.push(new NumberNode(token.getValue()));
            } else if (type == TokenType.REFERENCE) {
                operands.push(new ReferenceNode(token.getLexeme()));
            } else if (type == TokenType.LPAREN) {
                operators.push(type);
            } else if (type == TokenType.RPAREN) {
                collapseUntilLeftParen(operands, operators);
            } else if (isOperator(type)) {
                collapseByPrecedence(type, operands, operators);
                operators.push(type);
            } else if (type == TokenType.EOF) {
                break;
            } else {
                throw new FormulaException("Unknown type: " + type);
            }
        }

        while (!operators.isEmpty()) {
            TokenType top = operators.pop();
            if (top == TokenType.LPAREN) {
                throw new FormulaException("Missing ')'");
            }
            applyOperator(top, operands);
        }

        if (operands.size() != 1) {
            throw new FormulaException("Invalid expression");
        }
        return operands.pop();
    }

    private static void collapseUntilLeftParen(Stack<ExpressionNode> operands,
                                               Stack<TokenType> operators) {
        while (!operators.isEmpty() && operators.peek() != TokenType.LPAREN) {
            applyOperator(operators.pop(), operands);
        }
        if (operators.isEmpty()) {
            throw new FormulaException("Missing '('");
        }
        operators.pop();
    }

    private static void collapseByPrecedence(TokenType incoming,
                                             Stack<ExpressionNode> operands,
                                             Stack<TokenType> operators) {
        while (!operators.isEmpty()
                && isOperator(operators.peek())
                && precedence(operators.peek()) >= precedence(incoming)) {
            applyOperator(operators.pop(), operands);
        }
    }

    private static void applyOperator(TokenType operator,
                                      Stack<ExpressionNode> operands) {
        if (operands.size() < 2) {
            throw new FormulaException("Missing operand");
        }
        ExpressionNode right = operands.pop();
        ExpressionNode left = operands.pop();
        operands.push(new BinaryOpNode(operator, left, right));
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
}

package spreadsheet.formula.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.ast.BinaryOpNode;
import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.ast.FunctionCallNode;
import spreadsheet.formula.ast.NumberNode;
import spreadsheet.formula.ast.RangeNode;
import spreadsheet.formula.ast.ReferenceNode;
import spreadsheet.formula.lexer.Token;
import spreadsheet.formula.lexer.TokenType;

public final class ShuntingYardParser {
    private ShuntingYardParser() {
    }

    public static ExpressionNode parse(List<Token> tokens) {
        Stack<Token> operators = new Stack<>();
        Stack<ExpressionNode> operands = new Stack<>();
        Stack<Boolean> funcParens = new Stack<>();
        Stack<Integer> argCounts = new Stack<>();
        int functionDepth = 0;
        Token previous = null;

        for (Token token : tokens) {
            TokenType type = token.getType();
            // 21.12.2025 I switched to switch expression and a stack for better handling of different token types
            switch (type) {
                case NUMBER:
                    operands.push(new NumberNode(token.getValue()));
                    break;

                case REFERENCE:
                    operands.push(new ReferenceNode(token.getLexeme()));
                    break;

                case IDENT:
                    operators.push(token);
                    break;

                case LPAREN:
                    operators.push(token);
                    boolean isFunction = previous != null && previous.getType() == TokenType.IDENT;
                    funcParens.push(isFunction);
                    if (isFunction) {
                        argCounts.push(1);
                        functionDepth++;
                    }
                    break;

                case SEMICOLON:
                    // function argument separator 
                    if (funcParens.isEmpty() || !funcParens.peek()) {
                        throw new FormulaException("Unexpected ';'");
                    }
                    collapseUntilLeftParen(operands, operators);
                    if (operators.isEmpty() || operators.peek().getType() != TokenType.LPAREN) {
                        throw new FormulaException("Missing '('");
                    }
                    argCounts.push(argCounts.pop() + 1);
                    break;

                case RPAREN:
                    // general logic for closing parenthesis
                    // first collapse until left paren
                    // then check if it was a function call
                    // if so, pop function name and create function call node
                    // otherwise just continue
                    collapseUntilLeftParen(operands, operators);
                    if (operators.isEmpty() || operators.peek().getType() != TokenType.LPAREN) {
                        throw new FormulaException("Missing '('");
                    }
                    operators.pop();
                    boolean wasFunction = funcParens.pop();
                    if (wasFunction) {

                        if (operators.isEmpty() || operators.peek().getType() != TokenType.IDENT) {
                            throw new FormulaException("Missing function name");
                        }
                        Token funcToken = operators.pop();
                        int argCount = argCounts.pop();

                        if (operands.size() < argCount) {
                            throw new FormulaException("Missing function argument");
                        }
                        List<ExpressionNode> args = new ArrayList<>();
                        for (int i = 0; i < argCount; i++) {
                            args.add(0, operands.pop());
                        }
                        operands.push(new FunctionCallNode(funcToken.getLexeme(), args));
                        functionDepth--;
                    }
                    break;

                case COLON:
                    // range operator
                    if (functionDepth == 0) {
                        throw new FormulaException("Range only allowed inside functions");
                    }
                    collapseByPrecedence(token, operands, operators);
                    operators.push(token);
                    break;
                    
                // same case for arithmetic operators
                case PLUS:
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                    collapseByPrecedence(token, operands, operators);
                    operators.push(token);
                    break;
                case EOF:
                    previous = token;
                    break;
                default:
                    throw new FormulaException("Unknown type: " + type);
            }

            if (type == TokenType.EOF) {
                break;
            }
            previous = token;
        }

        while (!operators.isEmpty()) {
            Token top = operators.pop();
            if (top.getType() == TokenType.LPAREN) {
                throw new FormulaException("Missing ')'");
            }
            if (top.getType() == TokenType.IDENT) {
                throw new FormulaException("Missing '(' after function name");
            }
            applyOperator(top.getType(), operands);
        }

        if (operands.size() != 1) {
            throw new FormulaException("Invalid expression");
        }
        return operands.pop();
    }

    private static void collapseUntilLeftParen(Stack<ExpressionNode> operands,
                                               Stack<Token> operators) {
        // helper to collapse operators until left parenthesis is found 
        // to make sure parentheses are handled correctly
        while (!operators.isEmpty() && operators.peek().getType() != TokenType.LPAREN) {
            Token op = operators.pop();
            applyOperator(op.getType(), operands);
        }
    }

    private static void collapseByPrecedence(Token incoming,
                                             Stack<ExpressionNode> operands,
                                             Stack<Token> operators) {
        // helper to collapse operators based on precedence 
        while (!operators.isEmpty()
                && isOperator(operators.peek().getType())
                && precedence(operators.peek().getType()) >= precedence(incoming.getType())) {
            Token op = operators.pop();
            applyOperator(op.getType(), operands);
        }
    }

    private static void applyOperator(TokenType operator, Stack<ExpressionNode> operands) {
        // helper to apply an operator to the top two operands
        if (operands.size() < 2) {
            throw new FormulaException("Missing operand");
        }
        ExpressionNode right = operands.pop();
        ExpressionNode left = operands.pop();
        if (operator == TokenType.COLON) {
            if (!(left instanceof ReferenceNode) || !(right instanceof ReferenceNode)) {
                throw new FormulaException("Range endpoints must be cell references");
            }
            operands.push(new RangeNode((ReferenceNode) left, (ReferenceNode) right));
            return;
        }
        operands.push(new BinaryOpNode(operator, left, right));
    }

    private static boolean isOperator(TokenType type) {
        return type == TokenType.PLUS
            || type == TokenType.MINUS
            || type == TokenType.MULTIPLY
            || type == TokenType.DIVIDE
            || type == TokenType.COLON;
    }

    private static int precedence(TokenType type) {
        if (type == TokenType.COLON) {
            return 3;
        }
        if (type == TokenType.MULTIPLY || type == TokenType.DIVIDE) {
            return 2;
        }
        if (type == TokenType.PLUS || type == TokenType.MINUS) {
            return 1;
        }
        return 0;
    }
}

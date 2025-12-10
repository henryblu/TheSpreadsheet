package spreadsheet.formula.lexer;

import java.util.ArrayList;
import java.util.List;

public final class FormulaTokenizer {
    private FormulaTokenizer() {
    }

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int length = input.length();
        int i = 0;

        while (i < length) {
            char item = input.charAt(i);

            if (Character.isWhitespace(item)) {
                i++;
                continue;
            }

            if (Character.isDigit(item) || item == '.') {
                int firstNumberIndex = i;
                i++;
                while (i < length) {
                    char next = input.charAt(i);
                    if (Character.isDigit(next) || next == '.') {
                        i++;
                    } else {
                        break;
                    }
                }
                String number = input.substring(firstNumberIndex, i);
                double value = toNumber(number, firstNumberIndex);
                tokens.add(Token.number(value, number, firstNumberIndex));
                continue;
            }

            if (item == '+') {
                tokens.add(Token.operator(TokenType.PLUS, "+", i));
            } else if (item == '-') {
                tokens.add(Token.operator(TokenType.MINUS, "-", i));
            } else if (item == '*') {
                tokens.add(Token.operator(TokenType.MULTIPLY, "*", i));
            } else if (item == '/') {
                tokens.add(Token.operator(TokenType.DIVIDE, "/", i));
            } else if (item == '(') {
                tokens.add(Token.simple(TokenType.LPAREN, "(", i));
            } else if (item == ')') {
                tokens.add(Token.simple(TokenType.RPAREN, ")", i));
            } else {
                throw new LexerException("Unknown character '" + item + "' at position " + i);
            }
            i++;
        }

        tokens.add(Token.simple(TokenType.EOF, "", length));
        return tokens;
    }

    private static double toNumber(String text, int startIndex) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw new LexerException("Bad number near position " + startIndex);
        }
    }
}

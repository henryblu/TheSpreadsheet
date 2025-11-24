package spreadsheet.formula;
import java.util.ArrayList;
import java.util.List;

import spreadsheet.exceptions.FormulaException;

public class FormulaTokenizer {
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
                tokens.add(Token.number(value));
                continue;
            }

            if (item == '+') {
                tokens.add(Token.operator(TokenType.PLUS));
            } else if (item == '-') {
                tokens.add(Token.operator(TokenType.MINUS));
            } else if (item == '*') {
                tokens.add(Token.operator(TokenType.MULTIPLY));
            } else if (item == '/') {
                tokens.add(Token.operator(TokenType.DIVIDE));
            } else if (item == '(') {
                tokens.add(Token.simple(TokenType.LPAREN));
            } else if (item == ')') {
                tokens.add(Token.simple(TokenType.RPAREN));
            } else {
                throw new FormulaException ("Unknown character '" + item + "'");
            }
            i++;
        }

        tokens.add(Token.simple(TokenType.EOF));
        return tokens;
    }

    private static double toNumber(String text, int startIndex) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw new FormulaException("Bad number near position " + startIndex);
        }
    }
}

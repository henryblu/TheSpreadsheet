package spreadsheet.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import spreadsheet.exceptions.FormulaException;

class FormulaTest {

    @Test
    void tokenizerReadsNumbersAndOperators() {
        List<Token> tokens = FormulaTokenizer.tokenize("1+2");

        assertEquals(TokenType.NUMBER, tokens.get(0).getType());
        assertEquals(1.0, tokens.get(0).getValue());
        assertEquals(TokenType.PLUS, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals(2.0, tokens.get(2).getValue());
        assertEquals(TokenType.EOF, tokens.get(3).getType());
    }

    @Test
    void tokenizerSkipsSpacesAndReadsDecimals() {
        List<Token> tokens = FormulaTokenizer.tokenize(" 3.5  *  2 ");

        assertEquals(TokenType.NUMBER, tokens.get(0).getType());
        assertEquals(3.5, tokens.get(0).getValue());
        assertEquals(TokenType.MULTIPLY, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals(2.0, tokens.get(2).getValue());
        assertEquals(TokenType.EOF, tokens.get(3).getType());
    }

    @Test
    void evaluatorHandlesOrderOfOperations() {
        List<Token> tokens = FormulaTokenizer.tokenize("1+2*3");

        double result = FormulaEvaluator.evaluate(tokens);

        assertEquals(7.0, result);
    }

    @Test
    void evaluatorHandlesParentheses() {
        List<Token> tokens = FormulaTokenizer.tokenize("(4-1)*2");

        double result = FormulaEvaluator.evaluate(tokens);

        assertEquals(6.0, result);
    }

    @Test
    void evaluatorThrowsOnDivisionByZero() {
        List<Token> tokens = FormulaTokenizer.tokenize("1/0");

        assertThrows(FormulaException.class, () -> FormulaEvaluator.evaluate(tokens));
    }
}

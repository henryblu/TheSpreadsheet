package spreadsheet.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import spreadsheet.exceptions.FormulaException;
import spreadsheet.formula.ast.ExpressionNode;
import spreadsheet.formula.eval.FormulaEvaluator;
import spreadsheet.formula.lexer.FormulaTokenizer;
import spreadsheet.formula.lexer.Token;
import spreadsheet.formula.lexer.TokenType;
import spreadsheet.formula.parser.ShuntingYardParser;

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
        ExpressionNode node = ShuntingYardParser.parse(tokens);
        double result = FormulaEvaluator.evaluate(node);

        assertEquals(7.0, result);
    }

    @Test
    void evaluatorHandlesParentheses() {
        List<Token> tokens = FormulaTokenizer.tokenize("(4-1)*2");
        ExpressionNode node = ShuntingYardParser.parse(tokens);
        double result = FormulaEvaluator.evaluate(node);

        assertEquals(6.0, result);
    }

    @Test
    void evaluatorThrowsOnDivisionByZero() {
        List<Token> tokens = FormulaTokenizer.tokenize("1/0");

        assertThrows(FormulaException.class, () -> {
            ExpressionNode node = ShuntingYardParser.parse(tokens);
            FormulaEvaluator.evaluate(node);
        });
    }

    @Test
    void parserRejectsUnexpectedSemicolon() {
        List<Token> tokens = FormulaTokenizer.tokenize("1;2");

        assertThrows(FormulaException.class, () -> ShuntingYardParser.parse(tokens));
    }

    @Test
    void parserRejectsMissingClosingParen() {
        List<Token> tokens = FormulaTokenizer.tokenize("(1+2");

        assertThrows(FormulaException.class, () -> ShuntingYardParser.parse(tokens));
    }

    @Test
    void parserRejectsFunctionWithoutArguments() {
        List<Token> tokens = FormulaTokenizer.tokenize("SUM()");

        assertThrows(FormulaException.class, () -> ShuntingYardParser.parse(tokens));
    }

    @Test
    void parserRejectsRangeOutsideFunction() {
        List<Token> tokens = FormulaTokenizer.tokenize("A1:B2");

        assertThrows(FormulaException.class, () -> ShuntingYardParser.parse(tokens));
    }
}

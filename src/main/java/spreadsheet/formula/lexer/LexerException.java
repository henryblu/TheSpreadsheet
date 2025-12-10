package spreadsheet.formula.lexer;

import spreadsheet.exceptions.FormulaException;

public class LexerException extends FormulaException {
    public LexerException(String message) {
        super(message);
    }
}

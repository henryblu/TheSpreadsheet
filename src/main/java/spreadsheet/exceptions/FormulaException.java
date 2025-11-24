package spreadsheet.exceptions;

public class FormulaException extends RuntimeException {
    public FormulaException(String message) {
        super(message);
    }

    public FormulaException(String message, Throwable cause) {
        super(message, cause);
    }
}

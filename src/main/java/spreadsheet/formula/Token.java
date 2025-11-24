package spreadsheet.formula;
public class Token {
    private TokenType type;
    private double value;
    private boolean hasValue;

    public Token(TokenType type) {
        this.type = type;
        this.hasValue = false;
    }

    public Token(TokenType type, double value) {
        this.type = type;
        this.value = value;
        this.hasValue = true;
    }

    public TokenType getType() {
        return type;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public double getValue() {
        return value;
    }

    public static Token number(double value) {
        return new Token(TokenType.NUMBER, value);
    }

    public static Token operator(TokenType type) {
        return new Token(type);
    }

    public static Token simple(TokenType type) {
        return new Token(type);
    }
}

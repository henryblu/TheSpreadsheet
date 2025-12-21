package spreadsheet.formula.lexer;
public class Token {
    private final TokenType type;
    private final double value;
    private final boolean hasValue;
    private final String lexeme;
    private final int position;

    public Token(TokenType type) {
        this(type, 0.0, false, null, -1);
    }

    public Token(TokenType type, double value) {
        this(type, value, true, null, -1);
    }

    private Token(TokenType type,
                  double value,
                  boolean hasValue,
                  String lexeme,
                  int position) {
        this.type = type;
        this.value = value;
        this.hasValue = hasValue;
        this.lexeme = lexeme;
        this.position = position;
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

    public String getLexeme() {
        return lexeme;
    }

    public int getPosition() {
        return position;
    }

    public static Token number(double value, String lexeme, int position) {
        return new Token(TokenType.NUMBER, value, true, lexeme, position);
    }

    public static Token operator(TokenType type, String lexeme, int position) {
        return new Token(type, 0.0, false, lexeme, position);
    }

    public static Token simple(TokenType type, String lexeme, int position) {
        return new Token(type, 0.0, false, lexeme, position);
    }

    public static Token reference(String lexeme, int position) {
        return new Token(TokenType.REFERENCE, 0.0, false, lexeme, position);
    }
    public static Token identifier(String lexeme, int position) {
        return new Token(TokenType.IDENT, 0.0, false, lexeme, position);
    }
}

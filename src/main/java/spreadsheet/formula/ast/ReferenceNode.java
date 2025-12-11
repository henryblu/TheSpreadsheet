package spreadsheet.formula.ast;

public class ReferenceNode implements ExpressionNode {
    private final String reference;
    private final String columnLabel;
    private final int rowIndex;
    private final int columnIndex;

    public ReferenceNode(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Reference text cannot be empty");
        }
        String trimmed = text.trim();
        int digitOffset = firstDigitIndex(trimmed);
        if (digitOffset <= 0 || digitOffset == trimmed.length()) {
            throw new IllegalArgumentException("Invalid cell reference: " + text);
        }

        this.columnLabel = trimmed.substring(0, digitOffset).toUpperCase();
        String rowLabel = trimmed.substring(digitOffset);
        this.rowIndex = parseRow(rowLabel);
        this.columnIndex = parseColumn(columnLabel);
        this.reference = columnLabel + rowLabel;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public String getReference() {
        return reference;
    }

    private static int parseRow(String digits) {
        try {
            int value = Integer.parseInt(digits);
            if (value <= 0) {
                throw new IllegalArgumentException("Row must be positive: " + digits);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid row number: " + digits, ex);
        }
    }

    private static int parseColumn(String label) {
        int value = 0;
        for (int i = 0; i < label.length(); i++) {
            char ch = Character.toUpperCase(label.charAt(i));
            if (ch < 'A' || ch > 'Z') {
                throw new IllegalArgumentException("Invalid column label: " + label);
            }
            value = value * 26 + (ch - 'A' + 1);
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Column must be positive: " + label);
        }
        return value;
    }

    private static int firstDigitIndex(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}

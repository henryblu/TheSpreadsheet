package spreadsheet.formula.ast;

public class RangeBounds {
    private final int rowMin;
    private final int rowMax;
    private final int colMin;
    private final int colMax;

    public RangeBounds(int row1, int col1, int row2, int col2) {
        this.rowMin = Math.min(row1, row2);
        this.rowMax = Math.max(row1, row2);
        this.colMin = Math.min(col1, col2);
        this.colMax = Math.max(col1, col2);
    }

    public int getRowMin() { return rowMin; }
    public int getRowMax() { return rowMax; }
    public int getColMin() { return colMin; }
    public int getColMax() { return colMax; }
}

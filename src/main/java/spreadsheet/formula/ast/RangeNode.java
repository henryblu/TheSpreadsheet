package spreadsheet.formula.ast;

public class RangeNode implements ExpressionNode {
    private final ReferenceNode start;
    private final ReferenceNode end;

    public RangeNode(ReferenceNode start, ReferenceNode end) {
        this.start = start;
        this.end = end;
    }

    public ReferenceNode getStart() { return start; }
    public ReferenceNode getEnd() { return end; }

    public RangeBounds toBounds() {
        return new RangeBounds(
            start.getRowIndex(),
            start.getColumnIndex(),
            end.getRowIndex(),
            end.getColumnIndex()
        );
    }
}

package spreadsheet.formula.ast;

import java.util.HashSet;
import java.util.Set;

import spreadsheet.CellAddress;

public final class ReferenceCollector {
    private ReferenceCollector() {
    }

    public static Set<CellAddress> collect(ExpressionNode node) {
        Set<CellAddress> refs = new HashSet<>();
        collectInto(node, refs);
        return refs;
    }

    private static void collectInto(ExpressionNode node, Set<CellAddress> refs) {
        if (node == null) {
            return;
        }
        if (node instanceof ReferenceNode) {
            ReferenceNode ref = (ReferenceNode) node;
            refs.add(new CellAddress(ref.getRowIndex(), ref.getColumnIndex()));
            return;
        }
        if (node instanceof BinaryOpNode) {
            BinaryOpNode bin = (BinaryOpNode) node;
            collectInto(bin.getLeft(), refs);
            collectInto(bin.getRight(), refs);
        }
    }
}

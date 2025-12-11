package spreadsheet.formula.eval;

@FunctionalInterface
public interface CellLookup {
    double findCell(int rowIndex, int columnIndex);
}

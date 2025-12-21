package spreadsheet.formula.eval;

import java.util.OptionalDouble;
@FunctionalInterface
public interface CellLookup {
    double findCell(int rowIndex, int columnIndex);

    default OptionalDouble findCellOptional(int rowIndex, int columnIndex) {
        return OptionalDouble.of(findCell(rowIndex, columnIndex));
    }
}

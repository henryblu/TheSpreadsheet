# Deliverable 6 – Cell References Implementation

I added support for cell references in formulas. These references are now evaluated recursively. At this stage, circular references are still allowed. .

## What Changed

1. **Lexer**
   I extended `FormulaTokenizer` to recognize column-letter + row-digit patterns and emit a dedicated `REFERENCE` token. This lets the parser treat references the same way as other operands.

2. **AST and parser**
   I added `ReferenceNode` and updated `ShuntingYardParser` so it can push reference nodes onto the operand stack wherever a number is allowed. Each node parses its lexeme into 1-based row and column indices (for example, `AA1` becomes column 27, row 1).

3. **Evaluator**
   I introduced `CellLookup` and updated `FormulaEvaluator` so that when it visits a `ReferenceNode`, it calls `lookup.findCell(row, column)`. If no lookup is provided (such as when evaluating a standalone expression), it throws a `FormulaException`, which the UI displays as `#ERR`.

4. **Cell and spreadsheet wiring**
   Each `Cell` now keeps a reference to its parent `Spreadsheet`. It evaluates its display value using
   `FormulaEngine.evaluate(formula, spreadsheet::resolveCellValue)`
   and exposes `evaluateNumericValue()` so references pull either literal numbers or nested formula results.
   `Spreadsheet.resolveCellValue` locates the target cell, throws an error if it is empty or non-numeric, and returns the numeric value, recursing through formulas as needed.

5. **Tests**
   I added regression tests in `SpreadsheetTest` for references to literal values, references to formula cells, and the empty-cell error case.


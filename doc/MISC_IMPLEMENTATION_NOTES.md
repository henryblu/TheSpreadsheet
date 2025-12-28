# Misc Implementation Notes

## Blank cell creation on reference
- Location: `src/main/java/spreadsheet/Spreadsheet.java`
- Method: `resolveCellValue(int rowIndex, int columnIndex)`
- Change: When a referenced cell is missing from `cells`, a new `Cell` is created with empty content (`""`) and stored in `cells`.
- Effect: Empty/missing cells behave as `0.0` in numeric contexts (via `Cell.parseLiteral`), preserving prior formula behavior after empty/whitespace deletes.

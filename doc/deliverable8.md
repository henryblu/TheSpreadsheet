# Deliverable 8 – Functions and Ranges

For Deliverable 8, my goal was to extend the existing formula system with support for **functions and ranges**, while keeping the lexer → parser → AST → evaluator pipeline intact. This deliverable builds directly on the dependency graph and refresh logic introduced earlier and avoids introducing parallel parsing or evaluation paths.

The focus here is on **correctness, consistency, and incremental extension** of the existing design.

---

## Overview

The spreadsheet now supports:

* Functions: **SUM**, **MIN**, **MAX**, **AVERAGE**
* One or more arguments separated by `;`
* Arguments can be:

  * numeric literals
  * a single cell reference
  * a rectangular range (e.g. `A1:B3`)
  * another function call (nesting supported)

For example, the following formula is now valid:

```
=1 + A1*((SUM(A2:B5;AVERAGE(B6:D8);C1;27)/4)+(D6-D8))
```

All features are implemented using the existing AST-based evaluation flow, without special cases in `Cell` or `Spreadsheet`.

---

## Design decisions

### Ranges are only valid inside functions

Ranges are only allowed as function arguments.

This follows the specification and avoids ambiguous expressions such as `A1:B3 + 1`. Enforcing this rule at parse time keeps the evaluator simpler and makes error cases predictable. If a `:` is encountered outside a function argument list, the parser throws a `FormulaException`.

---

### Extending the existing Shunting Yard parser

I extended the current Shunting Yard–based parser instead of introducing a separate grammar.

This keeps a single parsing pipeline and allows arithmetic expressions, function calls, and nesting to work together naturally. Function names are pushed onto the operator stack, and function calls are constructed when a closing `)` is encountered. Argument counts are tracked explicitly and incremented on each `;`.

---

### Lexer support for identifiers, separators, and ranges

The lexer now emits:

* `IDENT` for function names
* `SEMICOLON` for argument separation
* `COLON` for range expressions

Letter-only sequences become identifiers, while letter+digit sequences become references. This distinction keeps the lexer simple and avoids grammar-level ambiguity.

---

### Explicit AST nodes for functions and ranges

I introduced two new AST nodes:

* `FunctionCallNode`
* `RangeNode`

Functions and ranges are treated as first-class syntax elements rather than being flattened into evaluator logic. This keeps both dependency tracking and evaluation explicit and testable.

`RangeNode` stores two `ReferenceNode` endpoints and delegates normalization to a small helper.

---

### Range normalization

Ranges are normalized so that bounds are always stored as top-left → bottom-right, regardless of input order.

This allows expressions like `B3:A1` to behave as expected and avoids special cases during iteration. The normalization logic computes min/max row and column values once and reuses them for evaluation and dependency collection.

---

### Handling empty and non-numeric cells in ranges

* **Empty cells** inside a range are ignored.
* **Non-numeric cells** still cause evaluation errors.

Ignoring empty cells avoids skewing `AVERAGE` by treating empty values as zero, while still allowing partially filled ranges. At the same time, referencing text cells in numeric contexts remains an error, preserving the spreadsheet’s numeric evaluation rules.

This behavior is implemented via `findCellOptional`, which returns an empty result for empty cells and throws for non-numeric values.

---

### Function names and language support

Functions are case-insensitive and support both Spanish and English names:

* `SUM` / `SUM`
* `AVERAGE` / `AVERAGE`

Internally, all functions are mapped to a single enum via `FunctionType.fromName`. This keeps the evaluator logic language-neutral while matching the specification.

---

### Dependency tracking with ranges

Ranges are expanded into explicit `CellAddress` sets during dependency collection.

This integrates cleanly with the existing dependency graph and ensures that any cell inside a range correctly triggers recalculation when edited. No changes were required to the graph structure itself—only to the reference collection step.

---

## Implementation summary

**Lexer**

* Added `IDENT`, `SEMICOLON`, and `COLON` tokens.
* Differentiates identifiers from references based on digits.

**Parser**

* Supports function calls and argument lists.
* Builds `FunctionCallNode` when closing a function scope.
* Restricts range usage to function arguments.

**Evaluator**

* Dispatches function evaluation via a dedicated function evaluator.
* Expands ranges at evaluation time while skipping empty cells.
* Throws errors for invalid numeric usage.

**Spreadsheet integration**

* `Spreadsheet` implements `CellLookup`.
* Dependency collection expands ranges into address sets.
* Recalculation uses the existing selective refresh logic.

---

## Tests

I added unit tests covering:

* Basic function calls with literals
* Ranges used in `MIN` and `MAX`
* `AVERAGE` with mixed arguments
* Nested functions inside arithmetic expressions


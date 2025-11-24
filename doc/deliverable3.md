# Deliverable 3 – Implementation Notes

For Deliverable 3, my goal was to add the first basic slice of formula support. Any cell whose text starts with = should be parsed, evaluated, and shown as a numeric result, while keeping file compatibility with Deliverable 2. I implemented this by adding a formula package and making small adjustments to Cell and Spreadsheet.

## Implementation Summary

* **Evaluation in the cell**
  Each Cell now stores both the raw content and a displayValue. When I call setContent, the cell runs a private evaluate() method. It checks for a leading = and, if present, runs the formula logic. The rest of the spreadsheet code doesn’t change; users now call getCellDisplayValue to show the result.

* **Error handling**
  If tokenizing or evaluating fails, FormulaException is caught inside Cell.evaluate(). In those cases, the cell shows #ERR. This keeps cell state predictable even when the formula is invalid.

* **Formula package structure**
  I added Token, TokenType, FormulaTokenizer, and `FormulaEvaluator` under `src/formula`. These are standalone helpers that will remain useful once I start adding dependency graphs or cross-cell references later.

## FormulaTokenizer

`FormulaTokenizer.tokenize(String)` scans the substring after `=` in a single pass. Each item becomes a `Token` instance that wraps a `TokenType` (enum) plus an optional `double` value. I use three static functions (`Token.number(value)`, `Token.operator(type)`, and `Token.simple(type)`) so callers never construct tokens incorrectly. The tokenizer emits, in order:

* numeric tokens (integers and decimals via `Double.parseDouble`)
* the operators `+`, `-`, `*`, `/`
* parentheses
* an explicit `EOF` token

The tokenizer ignores whitespace. It does not yet support unary operators, identifiers, or cell references. Invalid characters or malformed numbers raise a `FormulaException` with a position reference.

## FormulaEvaluator

`FormulaEvaluator.evaluate(List<Token>)` implements a stack-based evaluator similar to the shunting-yard approach:

1. Numbers go straight onto the value stack.
2. When an operator appears, I call `collapseByPrecedence` to apply any higher- or equal-precedence operators already on the operator stack.
3. Parentheses use `collapseUntilLeftParen` to enforce grouping.
4. After the final token, any remaining operators are applied.

`applyTopOperator` executes the four basic arithmetic operations. It checks for division by zero and missing operands and throws `FormulaException` if something is wrong. `Cell` converts those errors into `#ERR`.

## Current Limitations and Next Steps

Right now, formulas support only numeric literals and the operators `+`, `-`, `*`, and `/`. There’s no unary minus/plus, exponentiation, functions, or cross-cell references. I also output results using `Double.toString`.

Next we will integrate the provided parser and add unit tests.


# Deliverable 5 – Shunting-Yard Parser Refactor

Although this is when we should be implementing the provided Shunting Yard Algorithm, because I had already made the basics of one for the previous assignment I decided to just build my own now. So my goal is to replace the old “tokenize + evaluate immediately” helper classes with a small Shunting-Yard–based pipeline that produces a real AST. For now, this supports only numbers and binary operators, but it gives me the structure I need for references, loop detection, and functions later.

## Current State

* `FormulaTokenizer` only handles numbers and operators. It throws on any identifier, so the grammar is limited to basic arithmetic.
* `FormulaEvaluator` handles both precedence and evaluation directly on the token stream. There is no standalone AST or RPN representation that I can reuse for dependency checks or new features.
* The tests currently cover things like `1+2`, `3*5+4`, parentheses, whitespace handling, and error cases. These need to keep passing after the refactor.

## What Changed

1. **FormulaEngine façade**
   I removed direct knowledge of tokens and stacks from `Cell`. All evaluation now goes through `FormulaEngine.evaluate(String)`, which keeps future changes behind a single entry point.

2. **Standalone lexer**
   I moved `FormulaTokenizer`, `Token`, `TokenType`, and `LexerException` under `spreadsheet.formula.lexer`. Tokens can now carry lexeme and position metadata, but the existing constructors still work, so the lexer stays lightweight.

3. **Shunting-Yard parser**
   I added `ShuntingYardParser` to build an AST using operator and operand stacks instead of returning RPN. The logic follows the previous evaluator, but now it lives in the correct layer.

4. **Minimal AST nodes**
   I added `ExpressionNode`, `NumberNode`, and `BinaryOpNode`. This is enough to represent the current formulas and gives me a place to add `ReferenceNode`, `FunctionNode`, and others later.

5. **Evaluator rewrite**
   I rewrote `FormulaEvaluator` to walk the AST. Number nodes return their value, and binary nodes evaluate their children and apply the operator. The error behavior stays the same, but evaluation is now independent of parsing.

6. **Tests and documentation**
   I updated `FormulaTest` to run the full pipeline (tokenize → parse → evaluate). I also added a new flow diagram in `doc/plantuml-diagrams/formula_flow.puml`. The updated package layout is shown below.

## Expected Outcomes

* Existing numeric formulas still work. All current tests pass, and `FormulaEngine` now handles the entire flow.
* Adding references, cycle detection, ranges, or functions becomes incremental. I can extend token types, AST nodes, or evaluator behavior without rewriting the core algorithm.
* Lexer errors can now report positions, and the AST gives me a proper base for future diagnostics and features.

## Flow Overview

This flow is defined in `doc/plantuml-diagrams/formula_flow.puml`:

1. A `Cell` calls `FormulaEngine.evaluate(...)`.
2. The lexer returns tokens or throws `LexerException`.
3. `ShuntingYardParser` produces an `ExpressionNode` tree.
4. `FormulaEvaluator` walks the tree. Reference and function handling will plug into this step later.

## Source Layout

Current package structure:

```
src/main/java/spreadsheet/formula/
├─ FormulaEngine.java
├─ ast/
│  ├─ ExpressionNode.java
│  ├─ NumberNode.java
│  └─ BinaryOpNode.java
├─ eval/
│  └─ FormulaEvaluator.java
├─ lexer/
│  ├─ FormulaTokenizer.java
│  ├─ LexerException.java
│  ├─ Token.java
│  └─ TokenType.java
└─ parser/
   └─ ShuntingYardParser.java
```

This structure keeps lexing, parsing, AST construction, and evaluation cleanly separated, so I can add new features without reshaping the earlier layers.

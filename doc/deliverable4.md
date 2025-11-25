# Deliverable 4 – Maven + Unit Tests

For Deliverable 4 I reorganized the repo into a Maven-friendly layout and wrote the first round of JUnit coverage. Everything now lives under `src/main/java` and `src/test/java`, wired up through a `pom.xml` so `mvn clean test` builds and runs in one shot.

## Maven project layout

* Added a `pom.xml` with group/artifact metadata, Java 11 compiler settings, JUnit Jupiter 5.10.2, and Surefire 3.2.5 configured to stay off the module path.
* Moved production sources under `src/main/java/spreadsheet/**` and trimmed the old manual build artifacts; `bin/` is now optional instead of required.
* Updated the README with the new workflow, including how to run the CLI through Maven or directly from `target/classes`.

## Initial unit coverage

* `SpreadsheetTest` exercises the basic spreadsheet behaviors: blank cells stay empty, `setCellContent` drives row/column sizing, formulas show their evaluated display value (or `#ERR` on invalid input), and CSV save/load works via JUnit’s `@TempDir`.
* `FormulaTest` focuses on the tokenizer/evaluator pair, checking whitespace handling, decimals, precedence, parentheses, and error cases like division by zero to make sure `FormulaException` still bubbles up cleanly.

Next up I’ll extend the parser with the provided grammar, expand cross-cell references, and grow the test suite alongside each feature slice.

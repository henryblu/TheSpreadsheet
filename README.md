# TheSpreadsheet

A minimal spreadsheet app with basic formula support. Source lives in `src/main/java`, tests live in `src/test/java`, and build output goes to `bin/` (or `out/` for VS Code).

Documentation, deliverables, diagrams, and images are collected under `doc/`.

## Build and Test

1. Ensure Maven is installed and available on your `PATH` (`mvn -v` should work).
2. Run the unit tests:
   ```bash
   mvn clean test
   ```
3. Build without running tests:
   ```bash
   mvn clean package -D skipTests
   ```

## Running the CLI App

After a Maven build, the compiled classes sit under `target/classes`. You can run the CLI entrypoint either from Maven:

```bash
mvn exec:java -D exec.mainClass=spreadsheet.Main
```

or directly with `java` if you prefer manual compilation:

```bash
java -cp target/classes spreadsheet.Main
```

The previous manual workflow (`javac` into `bin/` and `java -cp bin spreadsheet.Main`) still works if you want to bypass Maven.

## Running the Web Demo (UI)

The browser demo lives under `docs/` and loads `docs/data/sample.s2v`, so it must be served over HTTP (not opened via `file://`).

```bash
cd docs
python -m http.server 8000
```

Then open `http://localhost:8000/` in your browser.

If you change Java code that affects the demo, regenerate the TeaVM output and replace `docs/spreadsheet.js` with the new build.

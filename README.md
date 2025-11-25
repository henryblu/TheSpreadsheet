# TheSpreadsheet

A minimal spreadsheet app with basic formula support. Source lives in `src/main/java`, tests live in `src/test/java`, and build output goes to `bin/` (or `out/` for VS Code).

Documentation, deliverables, diagrams, and images are collected under `doc/`.

## Build and Test with Maven

1. Ensure Maven is installed and available on your `PATH` (`mvn -v` should work).
2. From the project root, run:
   ```bash
   mvn clean test
   ```
   This compiles the main sources, runs the JUnit 5 suite, and produces a clean target directory.
3. To build an executable jar (without running tests), use:
   ```bash
   mvn clean package -D skipTests
   ```

## Running the App

After a Maven build, the compiled classes sit under `target/classes`. You can run the CLI entrypoint either from Maven:

```bash
mvn exec:java -D exec.mainClass=spreadsheet.Main
```

or directly with `java` if you prefer manual compilation:

```bash
java -cp target/classes spreadsheet.Main
```

The previous manual workflow (`javac` into `bin/` and `java -cp bin spreadsheet.Main`) still works if you want to bypass Maven.

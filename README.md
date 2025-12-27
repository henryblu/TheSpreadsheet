# TheSpreadsheet

A minimal spreadsheet application with basic formula support.

* Java source: `src/main/java`
* Tests: `src/test/java`
* Build output: `target/` (or `out/` when using VS Code)
* Documentation, diagrams, and images: `doc/`

A browser-based demo is available via GitHub Pages and works fully standalone.

---

## Web Demo (UI)

The web UI is published via GitHub Pages you can find the demo here: https://henryblu.github.io/TheSpreadsheet/

The UI loads a sample file from `docs/data/sample.s2v` and runs entirely in the browser.

---

## Build and Test (Java)

### Prerequisites

* Maven installed and available on your `PATH`

  ```bash
  mvn -v
  ```

### Run tests

```bash
mvn clean test
```

### Build without tests

```bash
mvn clean package -DskipTests
```

---

## Running the CLI Application

After building, compiled classes are located in `target/classes`.

### Run via Maven

```bash
mvn exec:java -Dexec.mainClass=spreadsheet.Main
```

### Run directly with Java

```bash
java -cp target/classes spreadsheet.Main
```

The older manual workflow (`javac` → `bin/`) still works if you prefer to bypass Maven.

---

## Running the UI Locally (Optional)

This is **only required if you want to modify the UI locally**.

Because the UI loads data files via HTTP, it must be served from a local web server (it will not work via `file://`).

```bash
cd docs
python -m http.server 8000
```

Then open:

```
http://localhost:8000/
```

Python is used **only** to serve static files during local development.

---

## Updating the Web Demo (TeaVM)

This section is relevant **only if you change Java code that affects the web UI**.

1. Build the Java project:

   ```bash
   mvn clean package -DskipTests
   ```

2. Run TeaVM using `spreadsheet.web.DemoEntry` as the entry point.
   This produces:

   ```
   target/teavm/spreadsheet.js
   ```

3. Replace the GitHub Pages bundle:

   ```bash
   cp target/teavm/spreadsheet.js docs/spreadsheet.js
   ```

After this, the updated UI can be served locally or published via GitHub Pages.


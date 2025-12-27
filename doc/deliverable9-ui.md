# Spreadsheet Project: Deliverable 9 UI Report

## Overview

This deliverable describes how I designed and implemented the final spreadsheet UI. I explain the platform choice, how the UI connects to the Java spreadsheet engine, and how UI use cases guided the design. I also document design trade-offs and changes made after testing real UI interactions.

---

## 1. UI Platform Choice and Design Rationale

I evaluated several UI approaches before choosing the final one:

* **CLI-only UI**
  Easy to build, but not suitable for inspecting or interacting with a spreadsheet.

* **Java Swing / JavaFX**
  Provided full control, but required more setup and caused platform issues for reviewers.

* **Web UI (HTML/CSS/JS)**
  Runs in any browser, is easy to review, and requires no local setup.

**Decision:** I chose a **web UI hosted on GitHub Pages**. This supports the main UI use cases, especially launching the app (UI-UC1) and reviewing spreadsheet behavior without installation.

This decision was driven by reviewer access, not by feature limitations.

---

## 2. UI Use Cases as the Design Driver

I translated system use cases into UI-specific ones in Deliverable 2. These UI use cases (found in deliverable91.md) directly shaped the UI structure and interaction model.

Key design goals from the use cases were:

* A usable grid on launch (UI-UC1)
* Clear file handling (UI-UC3, UI-UC4)
* Efficient keyboard and mouse interaction (UI-UC5–UI-UC7)
* Visible feedback for errors and invalid input (UI-UC10)
* Minimal UI elements with clear behavior

I grouped the use cases into implementation phases and implemented them incrementally.

---

## 3. App Lifecycle and Initial State (UI-UC1, UI-UC2)

On launch, the UI:

* Creates a new empty spreadsheet
* Renders an empty grid
* Automatically loads a sample S2V file

This supports **UI-UC1 (Launch application)** by giving the user a ready workspace and an example without manual steps.

Creating a new spreadsheet (UI-UC2) clears the model and refreshes the grid. I avoided multiple sheet tabs to keep the UI simple and focused.

---

## 4. Connecting the UI to the Java Spreadsheet Engine

To connect the web UI to the existing Java engine, I added a thin interface layer.

### Java–UI Bridge

* I created a `web` package with `DemoEntry.java`.
* This class exposes spreadsheet operations such as:

  * Loading S2V files
  * Reading and writing cell contents
  * Querying row and column counts

This layer exists only to support the UI and does not change core spreadsheet logic.

### TeaVM Compilation

* I compiled the Java engine using TeaVM.
* TeaVM produces `spreadsheet.js`, which exposes the Java methods to JavaScript.
* The output is placed in the `docs/` directory so it can be served by GitHub Pages.

### JavaScript Wrapper

* I added `docs/app.js` as a small wrapper layer.
* It handles grid rendering, selection state, editing, and file actions.
* All spreadsheet logic remains in Java.

This separation keeps UI code simple and avoids duplicating spreadsheet behavior.

---

## 5. File Operations and UI Constraints (UI-UC3, UI-UC4)

For file handling:

* **Open (UI-UC3)**
  Uses a browser file picker to load `.s2v` files and update the grid.

* **Save (UI-UC4)**
  Uses `showSaveFilePicker` when available, with a download fallback.

The UI does not expose S2V formatting rules. Parsing and validation happen in the model. The UI only shows success or error feedback.

This matches the design note that file format rules should stay out of the UI.

---

## 6. Grid Design and Interaction Model (UI-UC5–UI-UC7)

### Grid Size and Scrolling (UI-UC5)

The grid appears infinite but only stores cells once they contain data.
Rows and columns are added incrementally as the user scrolls.

This avoids pre-allocating a large grid and keeps rendering fast.

### Navigation (UI-UC5.5)

I implemented keyboard navigation using:

* Arrow keys
* Tab
* Enter

This was a deliberate design choice, as keyboard navigation is essential for spreadsheet use.

### Selection and Editing (UI-UC6, UI-UC7)

* Clicking a cell selects it and shows its raw content.
* Editing happens primarily through the formula bar.
* Changes are committed on Enter or when focus changes.

I avoided complex in-cell editing to reduce UI state issues and keep updates predictable.

---

## 7. Formula Handling and Error Feedback (UI-UC8–UI-UC10)

Formulas and ranges were already implemented in earlier deliverables, but the UI exposed new error cases.

During UI testing, I discovered cases where:

* Parsing errors were not clearly surfaced
* Evaluation errors triggered unexpected states in the UI

To fix this, I added **additional error handling in the Java code**, specifically around:

* Formula parsing failures
* Invalid references triggered by UI edits
* Errors that occur during partial or rapid updates

The UI shows errors through cell styling and status feedback, while the model remains responsible for detection.

This work was not fully anticipated earlier but became necessary once real UI interaction was in place.

---

## 8. Performance and Update Strategy

To avoid UI jitter and lag:

* The grid is updated incrementally
* Cells are updated individually
* The table structure is not rebuilt on every change

This directly follows the UI design note that updates must scale to larger sheets without slowing down the interface.

---

## 9. Final UI Behavior Summary

The final UI supports the following flow:

* Launch → empty grid → sample auto-load
* Select cells with mouse or keyboard
* View raw content and computed values
* Edit via formula bar
* Navigate efficiently using keys
* Scroll to expand the grid
* Load and save S2V files
* See clear error feedback when input fails

---

## 10. Key Files

* `src/main/java/spreadsheet/web/DemoEntry.java` – UI bridge
* `docs/index.html` – UI structure
* `docs/styles.css` – Styling
* `docs/app.js` – UI logic
* `docs/spreadsheet.js` – TeaVM output
* `docs/data/sample.s2v` – Sample data

---

## Conclusion

In this deliverable, I used UI use cases as the main design tool. They guided platform choice, layout, interaction style, and error handling. Building a real UI exposed edge cases that required changes in the Java code, especially around parsing and error reporting. The final result is a clear, minimal UI that reflects the spreadsheet model accurately and is easy for reviewers to inspect and test.

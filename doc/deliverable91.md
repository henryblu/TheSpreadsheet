# Spreadsheet Project: Deliverable 9.1 – UI Use Cases

## Introduction

This document describes the UI use cases that guided the spreadsheet interface design. I derived these from the core system use cases in Deliverable 1 and refined them during implementation. Each use case focuses on what the user does, what the UI shows, and how the UI responds.

---

## 1. UI Use Cases

Each UI use case describes a visible user action, the expected UI behavior, and the reason for the design.

### UI-UC1: Launch application

* **Description:**
  The UI opens with an empty grid and then automatically loads a demo spreadsheet.
* **Reasoning:**
  The user should see a working spreadsheet immediately, without manual setup.
* **Trigger:**
  User starts the application.

---

### UI-UC2: Create new spreadsheet

* **Description:**
  The UI creates a new empty spreadsheet and refreshes the grid.
* **Reasoning:**
  The user must be able to start a new sheet at any time.
* **Trigger:**
  User selects “New” from the UI.

---

### UI-UC3: Load spreadsheet (S2V)

* **Description:**
  The UI opens a file picker, loads an S2V file, and displays its contents in the grid.
* **Reasoning:**
  Users need to reopen saved spreadsheets.
* **Trigger:**
  User selects “Open” and chooses a file.

---

### UI-UC4: Save spreadsheet (S2V)

* **Description:**
  The UI saves the current spreadsheet to an S2V file and confirms completion.
* **Reasoning:**
  The UI must allow users to persist their work.
* **Trigger:**
  User selects “Save”.

---

### UI-UC5: Grid scrolling

* **Description:**
  The grid scrolls when content exceeds the visible area. The grid appears infinite, but cells are only stored in the model once they contain values.
* **Reasoning:**
  Users need to navigate large spreadsheets without performance issues.
* **Trigger:**
  User scrolls the grid.

---

### UI-UC5.5: Cell navigation

* **Description:**
  The selected cell moves using arrow keys, tab, and enter.
* **Reasoning:**
  Keyboard navigation is required for efficient spreadsheet use.
* **Trigger:**
  User presses navigation keys.

---

### UI-UC6: Select cell and view contents

* **Description:**
  Selecting a cell shows its raw content (such as a formula) and its computed value.
* **Reasoning:**
  Users need to inspect both input and result.
* **Trigger:**
  User clicks a cell.

---

### UI-UC7: Edit cell content

* **Description:**
  The user edits cell content through the cell or formula bar, and the grid updates after confirmation.
* **Reasoning:**
  Editing is the core interaction of the spreadsheet UI.
* **Trigger:**
  User types and confirms with Enter or by leaving the field.

---

### UI-UC8: Enter formulas and functions

* **Description:**
  The UI accepts formulas starting with `=` and displays computed results.
* **Reasoning:**
  Formula support is required for spreadsheet functionality.
* **Trigger:**
  User enters a formula.

---

### UI-UC9: Use ranges in formulas

* **Description:**
  The UI supports range syntax such as `A1:B3` and evaluates it correctly.
* **Reasoning:**
  Ranges are required for functions and calculations.
* **Trigger:**
  User enters a formula containing a range.

---

### UI-UC10: Display errors and diagnostics

* **Description:**
  The UI shows parse errors, invalid references, circular dependencies, and evaluation failures.
* **Reasoning:**
  Users need immediate feedback to correct input errors.
* **Trigger:**
  Errors occur during load or edit.

---

### UI-UC11: Confirm unsaved changes on exit

* **Description:**
  The UI warns about unsaved changes and allows the user to cancel or confirm exit.
* **Reasoning:**
  This prevents accidental data loss.
* **Trigger:**
  User exits with unsaved changes.

---

## 2. Implementation Groups

I grouped the UI use cases into logical implementation phases.

### Group 1: Already implemented

* UI-UC8: Enter formulas and functions
* UI-UC9: Use ranges in formulas

### Group 2: App lifecycle

* UI-UC1: Launch application
* UI-UC2: Create new spreadsheet

### Group 3: File operations

* UI-UC3: Load spreadsheet (S2V)
* UI-UC4: Save spreadsheet (S2V)

### Group 4: Grid interaction

* UI-UC5: Grid scrolling
* UI-UC5.5: Cell navigation
* UI-UC6: Select cell and view contents
* UI-UC7: Edit cell content

### Group 5: Feedback and safety

* UI-UC10: Display errors and diagnostics
* UI-UC11: Confirm unsaved changes on exit

---

## 3. UI Design Notes

* S2V parsing rules are handled in the model; the UI only exposes file actions and error feedback.
* Type coercion, formula evaluation, and dependency handling remain in the Java code.
* UI elements are kept minimal and focused on grid interaction, formula entry, and error visibility.
* Grid updates are incremental to avoid lag and visual jitter when working with larger sheets.

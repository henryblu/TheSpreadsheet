# Deliverable 7 – Dependency Graph and Refresh Logic

For Deliverable 7, my goal was to make formula evaluation more robust by introducing explicit dependency tracking between cells. This allows the spreadsheet to detect circular references early and to refresh only the cells affected by a change, instead of recomputing everything.

This deliverable focuses on **correctness and efficiency**, while keeping the design small and readable.

---

## Overview

I added a lightweight dependency graph to the `Spreadsheet`. The graph tracks:

* which cells a formula depends on
* which cells depend on a given cell

With this information, the spreadsheet can:

* reject circular references before committing an edit
* re-evaluate only the changed cell and its dependents

This fits naturally with the AST-based formula pipeline introduced in the previous deliverables.

---

## Dependency tracking

The `Spreadsheet` now owns two maps:

* **dependencies**: for each cell, the set of cells it references
* **dependents**: for each cell, the set of cells that reference it

Whenever a cell is edited and the content is a formula, the spreadsheet:

1. parses the formula into an AST
2. walks the AST to collect referenced cells
3. stores those references as the cell’s dependencies

If the formula is invalid, dependency collection safely returns an empty set so that bad input does not corrupt the graph.

---

## Loop detection

Before committing a formula change, the spreadsheet checks whether the new dependencies would introduce a cycle.

The check is done at update time rather than evaluation time. If a cycle is detected, the edit is rejected and a `FormulaException` is thrown. This ensures that:

* circular references never enter the spreadsheet state
* evaluation logic can assume the dependency graph is acyclic

This approach is more predictable than detecting loops during evaluation and avoids partially updated spreadsheets.

---

## Selective refresh

After a successful edit, the spreadsheet refreshes only the affected cells:

* the edited cell is recalculated
* all of its dependents are recalculated by walking the dependency graph

Each dependent is refreshed once, and the traversal always terminates because cycles are prevented earlier. This keeps recalculation efficient and prepares the design for larger spreadsheets and future GUI use.

---

## Design choices

I considered a simpler approach where each evaluation passes a stack of “currently evaluating” cells to detect loops. While simpler, that method:

* detects cycles only during evaluation, not before committing changes
* does not provide a clear way to refresh dependent cells
* does not scale well once ranges are introduced

Using an explicit dependency graph requires slightly more code, but it cleanly supports both loop detection and selective refresh, which are required for the next features.

---

## Preparing for ranges

At this stage, dependencies come only from single-cell references. The design intentionally treats dependencies as sets of cell addresses, so supporting ranges later will only require expanding a range into multiple addresses during dependency collection. The graph logic itself does not need to change.


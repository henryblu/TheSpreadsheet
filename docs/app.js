(() => {
  const gridHost = document.getElementById("grid");
  const formulaInput = document.getElementById("formula-input");
  const cellMeta = document.getElementById("cell-meta");
  const status = document.getElementById("status");
  const newButton = document.getElementById("new-sheet");
  const openButton = document.getElementById("open-sheet");
  const saveButton = document.getElementById("save-sheet");
  const fileInput = document.getElementById("file-input");

  const DEFAULT_ROWS = 24;
  const DEFAULT_COLS = 12;

  let api = null;
  let activeCell = null;
  let activeCellElement = null;
  let gridSize = { rows: DEFAULT_ROWS, cols: DEFAULT_COLS };
  let renderedSize = { rows: 0, cols: 0 };
  let gridTable = null;
  let gridHeadRow = null;
  let gridBody = null;
  let cellMatrix = [];
  let expandPending = false;
  let skipBlurApply = false;

  gridHost.tabIndex = 0;

  const columnLabel = (col) => {
    let current = col;
    let label = "";
    while (current > 0) {
      const remainder = (current - 1) % 26;
      label = String.fromCharCode(65 + remainder) + label;
      current = Math.floor((current - 1) / 26);
    }
    return label;
  };

  const resolveApi = () => {
    if (
      typeof window.getRowCount === "function" &&
      typeof window.getCellDisplayRC === "function" &&
      typeof window.setCellContentRC === "function"
    ) {
      return window;
    }
    const candidates = Object.keys(window);
    for (const key of candidates) {
      const value = window[key];
      if (!value || typeof value !== "object") continue;
      if (
        typeof value.getRowCount === "function" &&
        typeof value.getCellDisplayRC === "function" &&
        typeof value.setCellContentRC === "function"
      ) {
        return value;
      }
    }
    return null;
  };

  const updateStatus = () => {
    if (!api) return;
    try {
      const populated = getPopulatedCount();
      status.textContent = `Rows: ${gridSize.rows}, Cols: ${gridSize.cols}, Cells: ${populated}`;
    } catch (error) {
      status.textContent = `Rows: ${gridSize.rows}, Cols: ${gridSize.cols}`;
    }
  };

  const getPopulatedCount = () => {
    if (!api || typeof api.getNonEmptyCellAddresses !== "function") {
      return 0;
    }
    const list = api.getNonEmptyCellAddresses();
    return list ? list.split("\n").filter(Boolean).length : 0;
  };

  const getCellElement = (row, col) => {
    const rowCells = cellMatrix[row - 1];
    return rowCells ? rowCells[col - 1] : null;
  };

  const buildGrid = () => {
    const table = document.createElement("table");
    const thead = document.createElement("thead");
    const headRow = document.createElement("tr");
    const corner = document.createElement("th");
    corner.textContent = "";
    headRow.appendChild(corner);

    for (let col = 1; col <= gridSize.cols; col++) {
      const th = document.createElement("th");
      th.textContent = columnLabel(col);
      headRow.appendChild(th);
    }
    thead.appendChild(headRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    cellMatrix = Array.from({ length: gridSize.rows }, () =>
      Array.from({ length: gridSize.cols })
    );

    for (let row = 1; row <= gridSize.rows; row++) {
      const tr = document.createElement("tr");
      const rowHeader = document.createElement("td");
      rowHeader.textContent = row;
      rowHeader.className = "row-header";
      tr.appendChild(rowHeader);

      for (let col = 1; col <= gridSize.cols; col++) {
        const td = document.createElement("td");
        td.className = "cell";
        td.dataset.row = String(row);
        td.dataset.col = String(col);
        cellMatrix[row - 1][col - 1] = td;
        tr.appendChild(td);
      }
      tbody.appendChild(tr);
    }
    table.appendChild(tbody);

    gridHost.innerHTML = "";
    gridHost.appendChild(table);
    gridTable = table;
    gridHeadRow = headRow;
    gridBody = tbody;
    renderedSize = { ...gridSize };
    syncActiveCell();
  };

  const extendGridTo = (targetRows, targetCols) => {
    if (!gridTable || !gridHeadRow || !gridBody) {
      buildGrid();
      return;
    }

    const prevRows = renderedSize.rows;
    const prevCols = renderedSize.cols;
    const nextRows = Math.max(targetRows, renderedSize.rows);
    const nextCols = Math.max(targetCols, renderedSize.cols);

    if (nextCols > renderedSize.cols) {
      for (let col = renderedSize.cols + 1; col <= nextCols; col++) {
        const th = document.createElement("th");
        th.textContent = columnLabel(col);
        gridHeadRow.appendChild(th);
      }

      for (let row = 1; row <= renderedSize.rows; row++) {
        const tr = gridBody.rows[row - 1];
        for (let col = renderedSize.cols + 1; col <= nextCols; col++) {
          const td = document.createElement("td");
          td.className = "cell";
          td.dataset.row = String(row);
          td.dataset.col = String(col);
          cellMatrix[row - 1][col - 1] = td;
          tr.appendChild(td);
        }
      }
    }

    if (nextRows > renderedSize.rows) {
      for (let row = renderedSize.rows + 1; row <= nextRows; row++) {
        const tr = document.createElement("tr");
        const rowHeader = document.createElement("td");
        rowHeader.textContent = row;
        rowHeader.className = "row-header";
        tr.appendChild(rowHeader);

        const rowCells = Array.from({ length: nextCols });
        for (let col = 1; col <= nextCols; col++) {
          const td = document.createElement("td");
          td.className = "cell";
          td.dataset.row = String(row);
          td.dataset.col = String(col);
          rowCells[col - 1] = td;
          tr.appendChild(td);
        }
        cellMatrix[row - 1] = rowCells;
        gridBody.appendChild(tr);
      }
    }

    renderedSize = { rows: nextRows, cols: nextCols };
    if (api) {
      if (nextCols > prevCols) {
        for (let row = 1; row <= prevRows; row++) {
          for (let col = prevCols + 1; col <= nextCols; col++) {
            updateCellDisplay(row, col);
          }
        }
      }
      if (nextRows > prevRows) {
        for (let row = prevRows + 1; row <= nextRows; row++) {
          for (let col = 1; col <= nextCols; col++) {
            updateCellDisplay(row, col);
          }
        }
      }
    }
    syncActiveCell();
  };

  const ensureGridStructure = () => {
    if (!gridTable) {
      buildGrid();
      return;
    }
    if (
      renderedSize.rows !== gridSize.rows ||
      renderedSize.cols !== gridSize.cols
    ) {
      extendGridTo(gridSize.rows, gridSize.cols);
    }
  };

  const updateCellDisplay = (row, col) => {
    const cell = getCellElement(row, col);
    if (!cell || !api) return;
    cell.textContent = api.getCellDisplayRC(row, col) || "";
    if (api.isCellErrorRC && api.isCellErrorRC(row, col)) {
      cell.classList.add("error");
    } else {
      cell.classList.remove("error");
    }
  };

  const updateAllCells = () => {
    if (!api) return;
    ensureGridStructure();
    for (let row = 1; row <= gridSize.rows; row++) {
      for (let col = 1; col <= gridSize.cols; col++) {
        updateCellDisplay(row, col);
      }
    }
    syncActiveCell();
  };

  const syncActiveCell = () => {
    if (activeCellElement) {
      activeCellElement.classList.remove("active");
    }
    activeCellElement = null;
    if (activeCell) {
      const cell = getCellElement(activeCell.row, activeCell.col);
      if (cell) {
        activeCellElement = cell;
        activeCellElement.classList.add("active");
      }
    }
  };

  const clearSelection = () => {
    if (activeCellElement) {
      activeCellElement.classList.remove("active");
    }
    activeCell = null;
    activeCellElement = null;
    formulaInput.value = "";
    cellMeta.textContent = "No cell selected";
  };

  const setActiveCell = (row, col) => {
    if (activeCellElement) {
      activeCellElement.classList.remove("active");
    }
    activeCell = { row, col };
    activeCellElement = getCellElement(row, col);
    if (activeCellElement) {
      activeCellElement.classList.add("active");
    }
  };

  const selectCell = (row, col) => {
    setActiveCell(row, col);
    const content = api.getCellContentRC(row, col) || "";
    formulaInput.value = content;
    cellMeta.textContent = `${columnLabel(col)}${row}`;
  };

  const ensureGridForSelection = (row, col) => {
    if (row > gridSize.rows || col > gridSize.cols) {
      const prev = { ...gridSize };
      gridSize = {
        rows: Math.max(gridSize.rows, row),
        cols: Math.max(gridSize.cols, col),
      };
      ensureGridStructure();
      if (prev.rows !== gridSize.rows || prev.cols !== gridSize.cols) {
        updateStatus();
      }
    }
  };

  const scrollCellIntoView = (row, col) => {
    const cell = getCellElement(row, col);
    if (cell) {
      cell.scrollIntoView({ block: "nearest", inline: "nearest" });
    }
  };

  const moveSelectionBy = (rowDelta, colDelta) => {
    if (!activeCell) return;
    const nextRow = Math.max(1, activeCell.row + rowDelta);
    const nextCol = Math.max(1, activeCell.col + colDelta);
    ensureGridForSelection(nextRow, nextCol);
    selectCell(nextRow, nextCol);
    scrollCellIntoView(nextRow, nextCol);
  };

  const focusGrid = () => {
    try {
      gridHost.focus({ preventScroll: true });
    } catch (error) {
      gridHost.focus();
    }
  };

  const refreshGridSize = () => {
    const rows = Math.max(api.getRowCount(), gridSize.rows);
    const cols = Math.max(api.getColumnCount(), gridSize.cols);
    gridSize = { rows, cols };
  };

  const expandGrid = (rowsDelta, colsDelta) => {
    gridSize = {
      rows: gridSize.rows + rowsDelta,
      cols: gridSize.cols + colsDelta,
    };
    ensureGridStructure();
    updateStatus();
  };

  const handleGridScroll = () => {
    if (expandPending) return;
    const edgeBuffer = 80;
    const needsRows =
      gridHost.scrollTop + gridHost.clientHeight >=
      gridHost.scrollHeight - edgeBuffer;
    const needsCols =
      gridHost.scrollLeft + gridHost.clientWidth >=
      gridHost.scrollWidth - edgeBuffer;

    if (!needsRows && !needsCols) {
      return;
    }

    expandPending = true;
    requestAnimationFrame(() => {
      const rowStep = needsRows ? 10 : 0;
      const colStep = needsCols ? 6 : 0;
      if (rowStep || colStep) {
        expandGrid(rowStep, colStep);
      }
      expandPending = false;
    });
  };

  const applyFormulaInput = () => {
    if (!activeCell) return;
    api.setCellContentRC(activeCell.row, activeCell.col, formulaInput.value);
    refreshGridSize();
    ensureGridStructure();
    updateAllCells();
    updateStatus();
  };

  const resetSheet = () => {
    if (typeof api.reset === "function") {
      api.reset();
    } else if (typeof api.loadFromS2v === "function") {
      api.loadFromS2v("");
    }
    gridSize = { rows: DEFAULT_ROWS, cols: DEFAULT_COLS };
    clearSelection();
    ensureGridStructure();
    updateAllCells();
    updateStatus();
  };

  const fetchSample = async () => {
    const candidates = ["./data/sample.s2v", "data/sample.s2v", "./docs/data/sample.s2v"];
    for (const path of candidates) {
      try {
        const response = await fetch(path);
        if (!response.ok) {
          continue;
        }
        return response.text();
      } catch (error) {
        // Try the next candidate if fetch fails (e.g., relative path issues).
      }
    }
    throw new Error("Failed to load sample.s2v (check path or server)");
  };

  const readFileAsText = (file) =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result || "");
      reader.onerror = () =>
        reject(reader.error || new Error("Failed to read file."));
      reader.readAsText(file);
    });

  const formatCellForS2v = (content) => {
    if (!content) {
      return "";
    }
    if (content.startsWith("=")) {
      return content.replace(/;/g, ",");
    }
    return content;
  };

  const buildS2v = () => {
    if (!api) return "";
    const rows = api.getRowCount();
    const cols = api.getColumnCount();
    if (!rows || !cols) {
      return "";
    }
    const lines = [];
    for (let row = 1; row <= rows; row++) {
      const values = [];
      for (let col = 1; col <= cols; col++) {
        const content = api.getCellContentRC(row, col) || "";
        values.push(formatCellForS2v(content));
      }
      lines.push(values.join(";"));
    }
    return lines.join("\n");
  };

  const saveWithPicker = async () => {
    const s2v = buildS2v();
    if (typeof window.showSaveFilePicker !== "function") {
      return false;
    }
    const handle = await window.showSaveFilePicker({
      suggestedName: "spreadsheet.s2v",
      types: [
        {
          description: "S2V Spreadsheet",
          accept: { "text/plain": [".s2v"] },
        },
      ],
    });
    const writable = await handle.createWritable();
    await writable.write(s2v);
    await writable.close();
    status.textContent = `Saved ${handle.name}`;
    return true;
  };

  const downloadS2v = () => {
    const s2v = buildS2v();
    const blob = new Blob([s2v], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "spreadsheet.s2v";
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    status.textContent = "Saved spreadsheet.s2v";
  };

  const loadFromText = async (s2v) => {
    if (typeof api.loadFromS2v !== "function") {
      throw new Error("Spreadsheet API missing loadFromS2v");
    }
    api.loadFromS2v(s2v);
    await waitForData();
    if (getPopulatedCount() > 0) {
      selectCell(1, 1);
    } else {
      clearSelection();
    }
  };

  const loadSample = async () => {
    status.textContent = "Loading sample.s2v...";
    const s2v = await fetchSample();
    await loadFromText(s2v);
  };

  const hasLoadedSample = () => {
    try {
      return window.sessionStorage.getItem("spreadsheetDemoLoaded") === "1";
    } catch (error) {
      return false;
    }
  };

  const markSampleLoaded = () => {
    try {
      window.sessionStorage.setItem("spreadsheetDemoLoaded", "1");
    } catch (error) {
      // Ignore storage failures (private mode, disabled storage, etc.).
    }
  };

  const waitForData = async () => {
    const maxAttempts = 12;
    let attempts = 0;

    return new Promise((resolve) => {
      const tick = () => {
        attempts += 1;
        refreshGridSize();
        updateAllCells();
        updateStatus();
        if (getPopulatedCount() > 0 || attempts >= maxAttempts) {
          resolve();
          return;
        }
        requestAnimationFrame(tick);
      };

      requestAnimationFrame(tick);
    });
  };

  const bootstrap = async () => {
    api = resolveApi();
    if (!api) {
      status.textContent = "TeaVM output not loaded yet.";
      return;
    }

    try {
      resetSheet();
      if (!hasLoadedSample()) {
        await loadSample();
        markSampleLoaded();
        selectCell(1, 1);
      }
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "Failed to load spreadsheet.";
      status.textContent = message;
      console.error(error);
    }
  };

  gridHost.addEventListener("click", (event) => {
    const cell = event.target.closest(".cell");
    if (!cell) return;
    selectCell(Number(cell.dataset.row), Number(cell.dataset.col));
    focusGrid();
  });

  gridHost.addEventListener("scroll", handleGridScroll);
  gridHost.addEventListener("wheel", (event) => {
    if (expandPending) return;
    const needsRows =
      event.deltaY > 0 && gridHost.scrollHeight <= gridHost.clientHeight;
    const needsCols =
      (event.deltaX > 0 || (event.shiftKey && event.deltaY > 0)) &&
      gridHost.scrollWidth <= gridHost.clientWidth;

    if (!needsRows && !needsCols) {
      return;
    }

    event.preventDefault();
    expandPending = true;
    requestAnimationFrame(() => {
      const rowStep = needsRows ? 10 : 0;
      const colStep = needsCols ? 6 : 0;
      if (rowStep || colStep) {
        expandGrid(rowStep, colStep);
      }
      expandPending = false;
    });
  }, { passive: false });

  document.addEventListener("keydown", (event) => {
    if (event.defaultPrevented) {
      return;
    }
    if (event.ctrlKey || event.metaKey || event.altKey) {
      return;
    }
    if (document.activeElement === formulaInput) {
      return;
    }
    if (document.activeElement !== gridHost) {
      return;
    }

    const key = event.key;
    if (
      key === "ArrowUp" ||
      key === "ArrowDown" ||
      key === "ArrowLeft" ||
      key === "ArrowRight" ||
      key === "Tab" ||
      key === "Enter"
    ) {
      event.preventDefault();
      if (!activeCell) {
        ensureGridForSelection(1, 1);
        selectCell(1, 1);
      }
      if (key === "ArrowUp") moveSelectionBy(-1, 0);
      if (key === "ArrowDown") moveSelectionBy(1, 0);
      if (key === "ArrowLeft") moveSelectionBy(0, -1);
      if (key === "ArrowRight") moveSelectionBy(0, 1);
      if (key === "Tab") moveSelectionBy(0, event.shiftKey ? -1 : 1);
      if (key === "Enter") moveSelectionBy(1, 0);
      return;
    }

    if (key.length === 1 && activeCell) {
      event.preventDefault();
      formulaInput.focus();
      formulaInput.value = key;
      formulaInput.setSelectionRange(1, 1);
    }
  });

  formulaInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      event.stopPropagation();
      skipBlurApply = true;
      applyFormulaInput();
      moveSelectionBy(1, 0);
      formulaInput.blur();
      focusGrid();
    } else if (event.key === "Tab") {
      event.preventDefault();
      event.stopPropagation();
      skipBlurApply = true;
      applyFormulaInput();
      moveSelectionBy(0, event.shiftKey ? -1 : 1);
      formulaInput.blur();
      focusGrid();
    }
  });

  formulaInput.addEventListener("blur", () => {
    if (skipBlurApply) {
      skipBlurApply = false;
      return;
    }
    if (activeCell) {
      applyFormulaInput();
    }
  });

  if (newButton) {
    newButton.addEventListener("click", () => {
      resetSheet();
    });
  }

  if (openButton && fileInput) {
    openButton.addEventListener("click", () => {
      fileInput.click();
    });

    fileInput.addEventListener("change", async () => {
      const file = fileInput.files && fileInput.files[0];
      fileInput.value = "";
      if (!file) return;
      status.textContent = `Loading ${file.name}...`;
      try {
        const text = await readFileAsText(file);
        await loadFromText(text);
        status.textContent = `Loaded ${file.name}`;
      } catch (error) {
        const message =
          error instanceof Error ? error.message : "Failed to load file.";
        status.textContent = message;
        console.error(error);
      }
    });
  }

  if (saveButton) {
    saveButton.addEventListener("click", async () => {
      if (!api) {
        status.textContent = "Spreadsheet API not ready.";
        return;
      }
      try {
        const saved = await saveWithPicker();
        if (!saved) {
          downloadS2v();
        }
      } catch (error) {
        const message =
          error instanceof Error ? error.message : "Failed to save file.";
        status.textContent = message;
        console.error(error);
      }
    });
  }

  const start = () => {
    bootstrap();
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", start);
  } else {
    start();
  }
})();

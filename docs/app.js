(() => {
  const gridHost = document.getElementById("grid");
  const formulaInput = document.getElementById("formula-input");
  const cellMeta = document.getElementById("cell-meta");
  const status = document.getElementById("status");
  const resetButton = document.getElementById("reset-demo");

  let api = null;
  let activeCell = null;
  let gridSize = { rows: 12, cols: 8 };

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
    let populated = 0;
    if (typeof api.getNonEmptyCellAddresses === "function") {
      const list = api.getNonEmptyCellAddresses();
      populated = list ? list.split("\n").filter(Boolean).length : 0;
    }
    status.textContent = `Rows: ${gridSize.rows}, Cols: ${gridSize.cols}, Cells: ${populated}`;
  };

  const renderGrid = () => {
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
        td.textContent = api.getCellDisplayRC(row, col) || "";
        if (api.isCellErrorRC && api.isCellErrorRC(row, col)) {
          td.classList.add("error");
        }
        if (
          activeCell &&
          activeCell.row === row &&
          activeCell.col === col
        ) {
          td.classList.add("active");
        }
        tr.appendChild(td);
      }
      tbody.appendChild(tr);
    }
    table.appendChild(tbody);

    gridHost.innerHTML = "";
    gridHost.appendChild(table);
  };

  const selectCell = (row, col) => {
    activeCell = { row, col };
    const content = api.getCellContentRC(row, col) || "";
    formulaInput.value = content;
    cellMeta.textContent = `${columnLabel(col)}${row}`;
    renderGrid();
  };

  const refreshGridSize = () => {
    const rows = Math.max(api.getRowCount(), gridSize.rows);
    const cols = Math.max(api.getColumnCount(), gridSize.cols);
    gridSize = { rows, cols };
  };

  const applyFormulaInput = () => {
    if (!activeCell) return;
    api.setCellContentRC(activeCell.row, activeCell.col, formulaInput.value);
    refreshGridSize();
    renderGrid();
    updateStatus();
  };

  const loadSample = async () => {
    const response = await fetch("./data/sample.s2v");
    const s2v = await response.text();
    api.loadFromS2v(s2v);
    refreshGridSize();
    renderGrid();
    updateStatus();
    status.textContent = "Loaded sample.s2v";
  };

  const bootstrap = async () => {
    api = resolveApi();
    if (!api) {
      status.textContent = "TeaVM output not loaded yet.";
      return;
    }

    await loadSample();
    selectCell(1, 1);
  };

  gridHost.addEventListener("click", (event) => {
    const cell = event.target.closest(".cell");
    if (!cell) return;
    selectCell(Number(cell.dataset.row), Number(cell.dataset.col));
  });

  formulaInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      applyFormulaInput();
      formulaInput.blur();
    }
  });

  formulaInput.addEventListener("blur", () => {
    if (activeCell) {
      applyFormulaInput();
    }
  });

  resetButton.addEventListener("click", () => {
    loadSample();
  });

  window.addEventListener("load", bootstrap);
})();

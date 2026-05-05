// Ampersand frontend — Step 2: wire to the API.

const API = "/api/notes";
const CARDS_API = "/api/cards";

const state = {
    notes: [],
    activeNoteId: null,
    currentNote: null,
    activeFolder: "ZipCode",
    view: "notes",          // "notes" | "kanban"
    cards: [],
    activeCardId: null,
};

let saveTimer = null;

const els = {
    notesList: document.getElementById("notes-list"),
    noteDisplay: document.getElementById("note-display"),
    folderLabel: document.getElementById("current-folder-label"),
    jotsInput: document.getElementById("jots-input"),
    folderList: document.getElementById("folder-list"),
    jotsFolder: document.querySelector(".jots-folder"),
    newNoteBtn: document.getElementById("new-note-btn"),
    searchInput: document.getElementById("search-input"),
    kanbanPane: document.getElementById("kanban-pane"),
    noteContentPane: document.querySelector(".note-content-pane"),
    cardModal: document.getElementById("card-modal"),
    cardModalTitle: document.getElementById("card-modal-title"),
    cardModalMeta: document.getElementById("card-modal-meta"),
    cardModalDescription: document.getElementById("card-modal-description"),
    cardModalDueDate: document.getElementById("card-modal-duedate"),
    cardModalClose: document.getElementById("card-modal-close"),
    cardModalDelete: document.getElementById("card-modal-delete"),
    cardModalBackdrop: document.querySelector(".card-modal-backdrop"),
};

document.addEventListener("DOMContentLoaded", () => {
    loadNotes();
    loadCards();
    applyFolderColor("ZipCode");
    els.notesList.addEventListener("click", onNoteListClick);
    els.jotsInput.addEventListener("keydown", onJotsKeydown);
    els.folderList.addEventListener("click", onFolderClick);
    els.jotsFolder.addEventListener("click", () => selectFolder("Jots"));
    els.newNoteBtn.addEventListener("click", createNewNote);
    els.searchInput.addEventListener("input", scheduleSearch);
    updateClock();
    setInterval(updateClock, 1000);
    initPersonalize();
    initKanban();
    initCardModal();
});

// ---------- Kanban: load + render ----------

async function loadCards() {
    if (state.activeFolder === "Jots") {
        renderKanbanColumns([]);
        return;
    }
    try {
        const res = await fetch(`${CARDS_API}?board=${encodeURIComponent(state.activeFolder)}`);
        if (!res.ok) throw new Error("HTTP " + res.status);
        state.cards = await res.json();
        renderKanbanColumns(state.cards);
    } catch (err) {
        console.error("Failed to load cards:", err);
    }
}

function renderKanbanColumns(cards) {
    const columns = ["To Do", "In Progress", "Done"];
    columns.forEach(col => {
        const container = document.querySelector(`.kanban-cards[data-column="${col}"]`);
        if (!container) return;
        const inCol = cards
            .filter(c => (c.column || "To Do") === col)
            .sort((a, b) => (a.position || 0) - (b.position || 0));
        container.innerHTML = inCol.map(renderCardHtml).join("");
    });
    wireCardEvents();
}

function renderCardHtml(card) {
    const due = card.dueDate ? new Date(card.dueDate) : null;
    const overdue = due && due < new Date() && card.column !== "Done";
    const dueLabel = due
        ? `<span class="kanban-card-due${overdue ? " overdue" : ""}">${formatDueShort(due)}</span>`
        : "";
    const tags = (card.tags || [])
        .map(t => `<span class="kanban-card-tag">${escapeHtml(t)}</span>`)
        .join("");
    return `
        <div class="kanban-card" draggable="true" data-card-id="${escapeHtml(card.id)}">
            <div class="kanban-card-title">${escapeHtml(card.title || "Untitled")}</div>
            <div class="kanban-card-meta">${dueLabel}${tags}</div>
        </div>
    `;
}

function formatDueShort(date) {
    return date.toLocaleDateString([], { month: "short", day: "numeric" });
}

function wireCardEvents() {
    document.querySelectorAll(".kanban-card").forEach(cardEl => {
        cardEl.addEventListener("click", () => openCardModal(cardEl.dataset.cardId));
        cardEl.addEventListener("dragstart", onCardDragStart);
        cardEl.addEventListener("dragend", onCardDragEnd);
    });
}

// ---------- Drag and drop ----------

function initKanban() {
    document.querySelectorAll(".kanban-cards").forEach(zone => {
        zone.addEventListener("dragover", onDragOver);
        zone.addEventListener("dragleave", onDragLeave);
        zone.addEventListener("drop", onDrop);
    });
    document.querySelectorAll(".kanban-add-card").forEach(btn => {
        btn.addEventListener("click", () => addCardToColumn(btn.dataset.column));
    });
}

let draggedCardId = null;

function onCardDragStart(e) {
    draggedCardId = e.currentTarget.dataset.cardId;
    e.currentTarget.classList.add("dragging");
    e.dataTransfer.effectAllowed = "move";
}

function onCardDragEnd(e) {
    e.currentTarget.classList.remove("dragging");
    draggedCardId = null;
    document.querySelectorAll(".kanban-cards.drag-over").forEach(z => z.classList.remove("drag-over"));
}

function onDragOver(e) {
    e.preventDefault();
    e.currentTarget.classList.add("drag-over");
}

function onDragLeave(e) {
    e.currentTarget.classList.remove("drag-over");
}

async function onDrop(e) {
    e.preventDefault();
    e.currentTarget.classList.remove("drag-over");
    if (!draggedCardId) return;
    const newColumn = e.currentTarget.dataset.column;
    const card = state.cards.find(c => c.id === draggedCardId);
    if (!card) return;
    if (card.column === newColumn) return;

    try {
        const res = await fetch(`${CARDS_API}/${encodeURIComponent(draggedCardId)}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                title: card.title,
                description: card.description,
                boardId: card.boardId,
                column: newColumn,
                position: state.cards.filter(c => c.column === newColumn).length,
                tags: card.tags,
                dueDate: card.dueDate,
            }),
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        await loadCards();
    } catch (err) {
        console.error("Failed to move card:", err);
    }
}

// ---------- Add card ----------

async function addCardToColumn(column) {
    const title = prompt(`New card in "${column}":`);
    if (!title || !title.trim()) return;
    const id = "card-" + Date.now().toString(36);
    try {
        const res = await fetch(CARDS_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                id,
                title: title.trim(),
                description: "",
                boardId: state.activeFolder,
                column,
                position: state.cards.filter(c => c.column === column).length,
                tags: [],
                dueDate: null,
            }),
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        await loadCards();
    } catch (err) {
        console.error("Failed to add card:", err);
        alert("Couldn't add card: " + err.message);
    }
}

// ---------- Card edit modal ----------

let cardSaveTimer = null;

function initCardModal() {
    els.cardModalClose.addEventListener("click", closeCardModal);
    els.cardModalBackdrop.addEventListener("click", closeCardModal);
    els.cardModalTitle.addEventListener("input", scheduleCardSave);
    els.cardModalDescription.addEventListener("input", scheduleCardSave);
    els.cardModalDueDate.addEventListener("change", scheduleCardSave);
    els.cardModalDelete.addEventListener("click", deleteActiveCard);

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !els.cardModal.classList.contains("hidden")) {
            closeCardModal();
        }
    });
}

function openCardModal(cardId) {
    const card = state.cards.find(c => c.id === cardId);
    if (!card) return;
    state.activeCardId = cardId;
    els.cardModalTitle.value = card.title || "";
    els.cardModalDescription.value = card.description || "";
    els.cardModalDueDate.value = card.dueDate ? card.dueDate.slice(0, 16) : "";
    const modified = card.modified ? new Date(card.modified).toLocaleString() : "";
    els.cardModalMeta.textContent = `${card.column}${modified ? " · Modified " + modified : ""}`;
    els.cardModal.classList.remove("hidden");
    els.cardModalTitle.focus();
}

function closeCardModal() {
    els.cardModal.classList.add("hidden");
    state.activeCardId = null;
    if (cardSaveTimer) {
        clearTimeout(cardSaveTimer);
        saveActiveCard();
    }
}

function scheduleCardSave() {
    clearTimeout(cardSaveTimer);
    cardSaveTimer = setTimeout(saveActiveCard, 500);
}

async function saveActiveCard() {
    if (!state.activeCardId) return;
    const card = state.cards.find(c => c.id === state.activeCardId);
    if (!card) return;
    const due = els.cardModalDueDate.value;
    try {
        const res = await fetch(`${CARDS_API}/${encodeURIComponent(state.activeCardId)}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                title: els.cardModalTitle.value.trim() || "Untitled",
                description: els.cardModalDescription.value,
                boardId: card.boardId,
                column: card.column,
                position: card.position,
                tags: card.tags,
                dueDate: due ? due + ":00" : null,
            }),
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        await loadCards();
    } catch (err) {
        console.error("Failed to save card:", err);
    }
}

async function deleteActiveCard() {
    if (!state.activeCardId) return;
    if (!confirm("Delete this card?")) return;
    try {
        const res = await fetch(`${CARDS_API}/${encodeURIComponent(state.activeCardId)}`, {
            method: "DELETE",
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        closeCardModal();
        await loadCards();
    } catch (err) {
        console.error("Failed to delete card:", err);
        alert("Couldn't delete card: " + err.message);
    }
}

// ---------- Personalize: themes + wallpaper picker ----------

const THEMES = [
    {
        id: "aurora",
        name: "Aurora",
        accent: "109, 180, 224",
        bg: `linear-gradient(165deg, rgba(44, 82, 120, 0.45) 0%, rgba(15, 38, 64, 0.55) 100%),
             radial-gradient(ellipse 70% 50% at 25% 20%, rgba(140, 180, 215, 0.4) 0%, transparent 60%),
             radial-gradient(ellipse 60% 50% at 80% 80%, rgba(60, 100, 145, 0.5) 0%, transparent 65%),
             linear-gradient(165deg, #2c5278 0%, #1a3a5a 60%, #0f2640 100%)`,
        swatch: "linear-gradient(165deg, #4a7099, #1a3a5a, #0f2640)",
    },
    {
        id: "sunset",
        name: "Sunset",
        accent: "240, 168, 100",
        bg: `linear-gradient(165deg, rgba(120, 50, 60, 0.4) 0%, rgba(60, 25, 40, 0.55) 100%),
             radial-gradient(ellipse 70% 50% at 25% 20%, rgba(255, 180, 130, 0.4) 0%, transparent 60%),
             radial-gradient(ellipse 60% 50% at 80% 80%, rgba(180, 70, 90, 0.5) 0%, transparent 65%),
             linear-gradient(165deg, #c25a4a 0%, #6b2840 60%, #2a1020 100%)`,
        swatch: "linear-gradient(165deg, #e8945c, #c25a4a, #6b2840)",
    },
    {
        id: "forest",
        name: "Forest",
        accent: "142, 211, 163",
        bg: `linear-gradient(165deg, rgba(45, 74, 64, 0.45) 0%, rgba(20, 40, 32, 0.55) 100%),
             radial-gradient(ellipse 70% 50% at 25% 20%, rgba(160, 210, 175, 0.4) 0%, transparent 60%),
             radial-gradient(ellipse 60% 50% at 80% 80%, rgba(60, 110, 80, 0.5) 0%, transparent 65%),
             linear-gradient(165deg, #4a7060 0%, #2d4a40 60%, #1a3025 100%)`,
        swatch: "linear-gradient(165deg, #6ea48a, #2d4a40, #1a3025)",
    },
    {
        id: "deep-sea",
        name: "Deep Sea",
        accent: "95, 214, 221",
        bg: `linear-gradient(165deg, rgba(13, 58, 72, 0.5) 0%, rgba(5, 31, 40, 0.6) 100%),
             radial-gradient(ellipse 70% 50% at 25% 20%, rgba(140, 220, 230, 0.35) 0%, transparent 60%),
             radial-gradient(ellipse 60% 50% at 80% 80%, rgba(40, 130, 145, 0.5) 0%, transparent 65%),
             linear-gradient(165deg, #1a5a6a 0%, #0d3a48 60%, #051f28 100%)`,
        swatch: "linear-gradient(165deg, #2d8090, #0d3a48, #051f28)",
    },
    {
        id: "twilight",
        name: "Twilight",
        accent: "176, 140, 216",
        bg: `linear-gradient(165deg, rgba(45, 26, 74, 0.5) 0%, rgba(21, 8, 42, 0.6) 100%),
             radial-gradient(ellipse 70% 50% at 25% 20%, rgba(190, 160, 230, 0.4) 0%, transparent 60%),
             radial-gradient(ellipse 60% 50% at 80% 80%, rgba(95, 60, 145, 0.5) 0%, transparent 65%),
             linear-gradient(165deg, #4a2d6a 0%, #2d1a4a 60%, #15082a 100%)`,
        swatch: "linear-gradient(165deg, #8463b0, #2d1a4a, #15082a)",
    },
];

const STORAGE_KEY = "ampersand-theme";

function initPersonalize() {
    const btn = document.getElementById("personalize-btn");
    const panel = document.getElementById("personalize-panel");
    const presetsEl = document.getElementById("theme-presets");
    const customInput = document.getElementById("custom-wallpaper-url");
    const applyBtn = document.getElementById("apply-custom-btn");

    // Render swatches
    presetsEl.innerHTML = THEMES.map(t => `
        <button class="theme-swatch" data-theme="${t.id}" title="${t.name}" style="background: ${t.swatch};"></button>
    `).join("");

    presetsEl.addEventListener("click", (e) => {
        const swatch = e.target.closest(".theme-swatch");
        if (!swatch) return;
        const theme = THEMES.find(t => t.id === swatch.dataset.theme);
        if (theme) applyTheme(theme);
    });

    applyBtn.addEventListener("click", () => {
        const url = customInput.value.trim();
        if (!url) return;
        applyCustomWallpaper(url);
    });
    customInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") applyBtn.click();
    });

    btn.addEventListener("click", (e) => {
        e.stopPropagation();
        panel.classList.toggle("hidden");
    });
    document.addEventListener("click", (e) => {
        if (!panel.contains(e.target) && e.target !== btn) {
            panel.classList.add("hidden");
        }
    });

    // Load saved theme
    const saved = loadSavedTheme();
    if (saved) {
        if (saved.kind === "preset") {
            const theme = THEMES.find(t => t.id === saved.id);
            if (theme) applyTheme(theme, false);
        } else if (saved.kind === "custom") {
            applyCustomWallpaper(saved.url, saved.accent, false);
        }
    }
}

function applyTheme(theme, persist = true) {
    document.documentElement.style.setProperty("--accent-rgb", theme.accent);
    document.documentElement.style.setProperty("--body-bg", theme.bg);
    document.querySelectorAll(".theme-swatch").forEach(s => {
        s.classList.toggle("active", s.dataset.theme === theme.id);
    });
    if (persist) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify({ kind: "preset", id: theme.id }));
    }
}

function applyCustomWallpaper(url, accent, persist = true) {
    const tint = "linear-gradient(165deg, rgba(20, 30, 50, 0.4) 0%, rgba(10, 20, 35, 0.5) 100%)";
    document.documentElement.style.setProperty("--body-bg", `${tint}, url("${url}")`);
    if (accent) {
        document.documentElement.style.setProperty("--accent-rgb", accent);
    }
    document.querySelectorAll(".theme-swatch").forEach(s => s.classList.remove("active"));
    if (persist) {
        const currentAccent = getComputedStyle(document.documentElement).getPropertyValue("--accent-rgb").trim();
        localStorage.setItem(STORAGE_KEY, JSON.stringify({ kind: "custom", url, accent: currentAccent }));
    }
}

function loadSavedTheme() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

// ---------- Clock ----------

function updateClock() {
    const clock = document.getElementById("clock");
    if (!clock) return;
    const now = new Date();
    const time = now.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
    const date = now.toLocaleDateString([], { weekday: "long", month: "long", day: "numeric" });
    clock.querySelector(".clock-time").textContent = time;
    clock.querySelector(".clock-date").textContent = date;
}

// ---------- Search ----------

let searchTimer = null;
function scheduleSearch() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(runSearch, 200);
}

async function runSearch() {
    const q = els.searchInput.value.trim();
    if (!q) {
        renderNoteList();
        return;
    }
    try {
        const res = await fetch(`${API}/search?q=${encodeURIComponent(q)}`);
        if (!res.ok) throw new Error("HTTP " + res.status);
        const results = await res.json();
        renderSearchResults(results, q);
    } catch (err) {
        console.error("Search failed:", err);
    }
}

function renderSearchResults(results, query) {
    if (results.length === 0) {
        els.notesList.innerHTML = `<li class="note-item placeholder">No matches for "${escapeHtml(query)}".</li>`;
        return;
    }
    els.notesList.innerHTML = results.map(note => `
        <li class="note-item ${note.id === state.activeNoteId ? "active" : ""}" data-note-id="${escapeHtml(note.id)}">
            <div class="note-item-title">${escapeHtml(note.title || "(untitled)")}</div>
            <div class="note-item-preview">${escapeHtml(getPreview(note.content))}</div>
        </li>
    `).join("");
}

// ---------- New note ----------

async function createNewNote() {
    const targetFolder = state.activeFolder === "Jots" ? "ZipCode" : state.activeFolder;
    const id = "note-" + Date.now().toString(36);

    try {
        const res = await fetch(API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                id,
                title: "Untitled",
                content: "",
                author: null,
                tags: [],
                folder: targetFolder,
            }),
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        await loadNotes();
        if (state.activeFolder !== targetFolder) selectFolder(targetFolder);
        state.activeNoteId = id;
        await loadNoteDetail(id);

        const titleInput = els.noteDisplay.querySelector(".note-title-input");
        if (titleInput) {
            titleInput.focus();
            titleInput.select();
        }
    } catch (err) {
        console.error("Failed to create note:", err);
        alert("Couldn't create note: " + err.message);
    }
}

// ---------- Folders ----------

function onFolderClick(e) {
    const item = e.target.closest(".folder-item");
    if (!item) return;
    const folder = item.dataset.folder;
    if (folder) selectFolder(folder);
}

function selectFolder(folder) {
    state.activeFolder = folder;
    document.querySelectorAll(".folder-item").forEach(el => {
        el.classList.toggle("active", el.dataset.folder === folder);
    });
    els.jotsFolder.classList.toggle("active", folder === "Jots");
    document.querySelectorAll(".folder-label").forEach(el => {
        el.textContent = folder;
    });
    document.body.classList.toggle("jots-mode", folder === "Jots");
    applyFolderColor(folder);
    renderNoteList();
    if (folder !== "Jots") loadCards();
}

function applyFolderColor(folder) {
    const folderEl = document.querySelector(`.folder-item[data-folder="${folder}"]`);
    if (folderEl && folderEl.dataset.color) {
        document.documentElement.style.setProperty("--accent-rgb", folderEl.dataset.color);
    }
}

function notesInActiveFolder() {
    return state.notes.filter(n => {
        const f = n.folder || "ZipCode";
        return f === state.activeFolder;
    });
}

// ---------- Jots bar ----------

async function onJotsKeydown(e) {
    if (e.key !== "Enter") return;
    const text = els.jotsInput.value.trim();
    if (!text) return;

    const id = generateJotId(text);
    const title = text.length > 60 ? text.slice(0, 60) + "…" : text;

    try {
        const res = await fetch(API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                id,
                title,
                content: text,
                author: null,
                tags: ["jot"],
                folder: "Jots",
            }),
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        els.jotsInput.value = "";
        state.activeNoteId = id;
        await loadNotes();
        selectFolder("Jots");
        await loadNoteDetail(id);
    } catch (err) {
        console.error("Failed to save jot:", err);
        alert("Couldn't save jot: " + err.message);
    }
}

function generateJotId(text) {
    const slug = text.toLowerCase()
        .replace(/[^a-z0-9 ]/g, "")
        .trim()
        .split(/\s+/)
        .slice(0, 5)
        .join("-")
        .slice(0, 40);
    const ts = Date.now().toString(36);
    return `${slug || "jot"}-${ts}`;
}

async function loadNotes() {
    try {
        const res = await fetch(API);
        if (!res.ok) throw new Error("HTTP " + res.status);
        state.notes = await res.json();
        renderNoteList();
    } catch (err) {
        els.notesList.innerHTML = `<li class="note-item placeholder">Couldn't load notes: ${escapeHtml(err.message)}</li>`;
    }
}

function renderNoteList() {
    const visible = notesInActiveFolder();
    if (visible.length === 0) {
        const msg = state.activeFolder === "Jots"
            ? "No jots yet — type above and hit Enter."
            : `No notes in ${state.activeFolder} yet.`;
        els.notesList.innerHTML = `<li class="note-item placeholder">${escapeHtml(msg)}</li>`;
        return;
    }
    els.notesList.innerHTML = visible.map(note => `
        <li class="note-item ${note.id === state.activeNoteId ? "active" : ""}" data-note-id="${escapeHtml(note.id)}">
            <div class="note-item-title">${escapeHtml(note.title || "(untitled)")}</div>
            <div class="note-item-preview">${escapeHtml(getPreview(note.content))}</div>
        </li>
    `).join("");
}

function getPreview(content) {
    if (!content) return "";
    const firstLine = content.split("\n").map(l => l.trim()).find(l => l.length > 0) || "";
    return firstLine.length > 80 ? firstLine.slice(0, 80) + "…" : firstLine;
}

async function onNoteListClick(e) {
    const item = e.target.closest(".note-item");
    if (!item || item.classList.contains("placeholder")) return;
    const id = item.dataset.noteId;
    state.activeNoteId = id;
    renderNoteList();
    await loadNoteDetail(id);
}

async function loadNoteDetail(id) {
    try {
        const res = await fetch(`${API}/${encodeURIComponent(id)}`);
        if (!res.ok) throw new Error("HTTP " + res.status);
        const note = await res.json();
        renderNoteDetail(note);
    } catch (err) {
        els.noteDisplay.innerHTML = `<p class="placeholder-text">Couldn't load: ${escapeHtml(err.message)}</p>`;
    }
}

function renderNoteDetail(note) {
    state.currentNote = { ...note, id: state.activeNoteId };

    const modified = note.modified ? new Date(note.modified).toLocaleString() : "";
    const tags = note.tags && note.tags.length
        ? `<span class="tags">${note.tags.map(t => `<span class="tag">${escapeHtml(t)}</span>`).join("")}</span>`
        : "";
    const metaPieces = [];
    if (note.author) metaPieces.push(`By ${escapeHtml(note.author)}`);
    if (modified) metaPieces.push(`Modified ${escapeHtml(modified)}`);
    const metaLine = metaPieces.join(" · ");

    els.noteDisplay.innerHTML = `
        <input class="note-title-input" type="text" value="${escapeAttr(note.title || "")}" placeholder="Untitled" />
        <div class="note-meta">${metaLine}${tags}</div>
        <textarea class="note-body-input" placeholder="Start writing…">${escapeHtml(note.content || "")}</textarea>
        <div class="note-actions">
            <button class="delete-btn" type="button">Delete note</button>
        </div>
    `;

    const titleInput = els.noteDisplay.querySelector(".note-title-input");
    const bodyInput = els.noteDisplay.querySelector(".note-body-input");
    const deleteBtn = els.noteDisplay.querySelector(".delete-btn");

    titleInput.addEventListener("input", scheduleSave);
    bodyInput.addEventListener("input", scheduleSave);
    deleteBtn.addEventListener("click", deleteCurrentNote);
}

// ---------- Edit (auto-save) ----------

function scheduleSave() {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(saveCurrentNote, 600);
}

async function saveCurrentNote() {
    if (!state.currentNote) return;
    const titleInput = els.noteDisplay.querySelector(".note-title-input");
    const bodyInput = els.noteDisplay.querySelector(".note-body-input");
    if (!titleInput || !bodyInput) return;

    const id = state.currentNote.id;
    const newTitle = titleInput.value.trim() || "Untitled";
    const newContent = bodyInput.value;

    try {
        const res = await fetch(`${API}/${encodeURIComponent(id)}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                id,
                title: newTitle,
                content: newContent,
                author: state.currentNote.author,
                tags: state.currentNote.tags,
            }),
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        state.currentNote.title = newTitle;
        state.currentNote.content = newContent;
        const listItem = state.notes.find(n => n.id === id);
        if (listItem) {
            listItem.title = newTitle;
            listItem.content = newContent;
            renderNoteList();
        }
    } catch (err) {
        console.error("Save failed:", err);
    }
}

// ---------- Delete ----------

async function deleteCurrentNote() {
    if (!state.currentNote) return;
    const id = state.currentNote.id;
    const label = state.currentNote.title || id;
    if (!confirm(`Delete "${label}"? This can't be undone.`)) return;

    try {
        const res = await fetch(`${API}/${encodeURIComponent(id)}`, {
            method: "DELETE",
        });
        if (!res.ok) throw new Error("HTTP " + res.status);
        state.activeNoteId = null;
        state.currentNote = null;
        await loadNotes();
        els.noteDisplay.innerHTML = '<p class="placeholder-text">Pick a note to read.</p>';
    } catch (err) {
        console.error("Delete failed:", err);
        alert("Couldn't delete: " + err.message);
    }
}

function escapeAttr(s) {
    return (s ?? "")
        .replace(/&/g, "&amp;")
        .replace(/"/g, "&quot;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

function escapeHtml(s) {
    const div = document.createElement("div");
    div.textContent = s ?? "";
    return div.innerHTML;
}

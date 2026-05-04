// Ampersand frontend — Step 2: wire to the API.

const API = "/api/notes";

const state = {
    notes: [],
    activeNoteId: null,
    currentNote: null,
    activeFolder: "Work",
};

let saveTimer = null;

const els = {
    notesList: document.getElementById("notes-list"),
    noteDisplay: document.getElementById("note-display"),
    folderLabel: document.getElementById("current-folder-label"),
    jotsInput: document.getElementById("jots-input"),
    folderList: document.getElementById("folder-list"),
    jotsFolder: document.querySelector(".jots-folder"),
};

document.addEventListener("DOMContentLoaded", () => {
    loadNotes();
    els.notesList.addEventListener("click", onNoteListClick);
    els.jotsInput.addEventListener("keydown", onJotsKeydown);
    els.folderList.addEventListener("click", onFolderClick);
    els.jotsFolder.addEventListener("click", () => selectFolder("Jots"));
});

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
    els.folderLabel.textContent = folder;
    renderNoteList();
}

function notesInActiveFolder() {
    return state.notes.filter(n => {
        const f = n.folder || "Work";
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

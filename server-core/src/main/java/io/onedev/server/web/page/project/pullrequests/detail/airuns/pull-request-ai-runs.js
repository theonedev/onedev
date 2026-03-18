onedev.server.aiRun = {
_guidanceState: {},

initTerminal: function(containerId, sourceId) {
var container = document.getElementById(containerId);
var source = document.getElementById(sourceId);
if (!container || !source || typeof Terminal === "undefined" || typeof FitAddon === "undefined")
return;

if (container._aiRunTerminal) {
container._aiRunTerminal.term.dispose();
container._aiRunTerminal = undefined;
}

container.innerHTML = "";

var term = new Terminal({
convertEol: true,
cursorBlink: false,
disableStdin: true,
fontSize: 13,
theme: {
background: "#111217",
foreground: "#f5f8fa"
}
});
var fitAddon = new FitAddon.FitAddon();
term.loadAddon(fitAddon);
term.open(container);
fitAddon.fit();
term.write(source.textContent || "");
term.scrollToBottom();
container._aiRunTerminal = {
term: term,
fitAddon: fitAddon
};
},

initTerminalIfNeeded: function(detailId) {
var detail = document.getElementById(detailId);
if (!detail)
return;
var wrapper = detail.querySelector("[id$='-tw']");
if (!wrapper || wrapper.style.display === "none")
return;
var termEl = wrapper.querySelector(".ai-run-terminal");
if (!termEl || termEl._aiRunTerminal)
return;
var logSource = termEl.getAttribute("data-log-source");
if (logSource)
this.initTerminal(termEl.id, logSource);
},

autoSizeTextarea: function(input) {
if (!input)
return;
input.style.height = "auto";
input.style.height = Math.min(input.scrollHeight, 224) + "px";
},

wireComposer: function(root) {
var self = this;
root.querySelectorAll(".ai-run-input-form textarea").forEach(function(input) {
if (input.dataset.aiRunBound === "true")
return;
input.dataset.aiRunBound = "true";
self.autoSizeTextarea(input);
input.addEventListener("input", function() {
self.autoSizeTextarea(input);
});
input.addEventListener("keydown", function(e) {
if (e.key === "Enter" && !e.shiftKey) {
e.preventDefault();
var submit = input.closest("form").querySelector("button[type='submit']");
if (submit)
submit.click();
}
});
});
},

scrollFeeds: function(root) {
root.querySelectorAll(".ai-run-conversation").forEach(function(feed) {
feed.scrollTop = feed.scrollHeight;
});
},

initVisibleTerminals: function(root) {
if (root.id && root.id.endsWith("detail"))
this.initTerminalIfNeeded(root.id);
root.querySelectorAll("[id$='detail']").forEach(function(detail) {
onedev.server.aiRun.initTerminalIfNeeded(detail.id);
});
},

preserveGuidance: function(containerId) {
var container = document.getElementById(containerId);
if (!container)
return;

var state = {};
container.querySelectorAll("textarea[data-ai-guidance-key]").forEach(function(input) {
var key = input.getAttribute("data-ai-guidance-key");
state[key] = {
value: input.value,
focused: document.activeElement === input,
selectionStart: input.selectionStart,
selectionEnd: input.selectionEnd
};
});
this._guidanceState[containerId] = state;
},

restoreGuidance: function(containerId) {
var container = document.getElementById(containerId);
var state = this._guidanceState[containerId];
if (!container || !state)
return;

container.querySelectorAll("textarea[data-ai-guidance-key]").forEach(function(input) {
var key = input.getAttribute("data-ai-guidance-key");
var saved = state[key];
if (!saved)
return;
input.value = saved.value;
if (saved.focused) {
input.focus();
if (typeof input.setSelectionRange === "function")
input.setSelectionRange(saved.selectionStart, saved.selectionEnd);
}
onedev.server.aiRun.autoSizeTextarea(input);
});
delete this._guidanceState[containerId];
},

insertSlashCommand: function(button) {
if (!button)
return false;
var form = button.closest("form");
if (!form)
return false;
var input = form.querySelector("textarea[data-ai-guidance-key], textarea");
var command = button.getAttribute("data-ai-slash-command");
if (!input || !command)
return false;
if (!input.value.trim())
input.value = command;
else if (!input.value.trim().startsWith("/"))
input.value = command + input.value.trim();
else
input.value = command;
this.autoSizeTextarea(input);
input.focus();
if (typeof input.setSelectionRange === "function") {
var end = input.value.length;
input.setSelectionRange(end, end);
}
return false;
},

onDomReady: function(containerId) {
var root = containerId ? document.getElementById(containerId) : document;
if (!root)
return;
this.wireComposer(root);
this.scrollFeeds(root);
this.initVisibleTerminals(root);
}
};

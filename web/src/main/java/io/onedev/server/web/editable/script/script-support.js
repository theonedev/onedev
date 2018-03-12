onedev.server.scriptSupport = {
	onEditorDomReady: function(inputId) {
		var cm = CodeMirror.fromTextArea(document.getElementById(inputId), {
			indentWithTabs: true,
			indentUnit: 4,
			tabSize: 4,
			theme: "eclipse",
			lineNumbers: true,
			styleActiveLine: true,
			styleSelectedText: true,
			foldGutter: true,
			matchBrackets: true,
			scrollbarStyle: "simple",
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
			highlightIdentifiers: {delay: 500}
		});
		onedev.server.codemirror.setMode(cm, ".groovy");
		
		var $input = $("#" + inputId);
		cm.on("change", function() {
			cm.save();
			onedev.server.form.markDirty($input.closest("form"));
		});
	},
	onViewerDomReady: function(inputId) {
		var cm = CodeMirror.fromTextArea(document.getElementById(inputId), {
			readOnly: true,
			indentWithTabs: true,
			indentUnit: 4,
			tabSize: 4,
			theme: "eclipse",
			lineNumbers: true,
			lineWrapping: true,
			styleActiveLine: true,
			styleSelectedText: true,
			foldGutter: true,
			matchBrackets: true,
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
			highlightIdentifiers: {delay: 500}
		});
		onedev.server.codemirror.setMode(cm, ".groovy");
	}
}
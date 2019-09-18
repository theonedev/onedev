onedev.server.scriptSupport = {
	onEditorDomReady: function(inputId, modeName) {
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
		onedev.server.codemirror.setModeByName(cm, modeName);
		
		var $input = $("#" + inputId);
		cm.on("change", function() {
			cm.save();
			onedev.server.form.markDirty($input.closest("form"));
		});
	    onedev.server.scriptSupport.trackWidth(inputId);
    },
	onViewerDomReady: function(inputId, modeName) {
		var cm = CodeMirror.fromTextArea(document.getElementById(inputId), {
			readOnly: true,
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

        onedev.server.codemirror.setModeByName(cm, modeName);

        onedev.server.scriptSupport.trackWidth(inputId);
	},
    trackWidth: function(inputId) {
        var $cm = $("#" + inputId).next();
        var cm = $cm[0].CodeMirror;
        function setWidth() {
            var $cm = $("#"+inputId).next();
            cm.setSize(1, null);  // shrink temporarily in order to get original parent width
            cm.setSize($cm.parent().width(), null);
            cm.refresh();
        }
        setWidth();
        $(window).resize(setWidth);
    }
}
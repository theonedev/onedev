onedev.server.codeSupport = {
	onEditorLoad: function(inputId, modeName, varQueryCallback) {
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
			
			cm.on("keypress", function(cm, event) {
				if (event.key == "@") {
					// Give codemirror a chance to handle keypress first
					setTimeout(function() {
				    	var cursor = cm.getCursor();
				    	var line = cursor.line;
				    	var start = cursor.ch;
						
				    	var beforeCursor = cm.doc.getLine(line).substring(0, start);
				    	
				    	var escapeFiltered = beforeCursor.replace("@@", "");
				    	if ((escapeFiltered.match(/@/g) || []).length % 2 == 1) { // only show hint when type left @
				    		function hint(cm, showHintCallback) {
						    	var end = cm.getCursor().ch;
						    	var matchWith = cm.doc.getLine(line).substring(start, end);
						    	$input.data("showHintCallback", showHintCallback);
						    	varQueryCallback(matchWith, line, start);
							}
				    		hint.async = true;
							CodeMirror.showHint(cm, hint, {completeSingle: false});
				    	}
					}, 0);
				}
			});
			
			$input.on("beforeDelete", function() {
				cm.toTextArea();
			});
    },
    showVariables: function(inputId, variables, line, start) {
    	var $input = $("#" + inputId);
		var cm = $input.next(".CodeMirror")[0].CodeMirror;		
    	$input.data("showHintCallback")({
    		list: variables,
    		from: CodeMirror.Pos(line, start-1),
    		to: CodeMirror.Pos(line, cm.getCursor().ch)
    	});
    },
	onViewerLoad: function(inputId, modeName) {
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
	}
}

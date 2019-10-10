onedev.server.codeSupport = {
	onEditorDomReady: function(inputId, modeName, varQueryCallback) {
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
		
		cm.on("keyup", function(cm, event) {
			if (event.keyCode == 50) {
		    	var cursor = cm.getCursor();
		    	var line = cursor.line;
		    	var start = cursor.ch;
		    	
		    	var beforeCursor = cm.doc.getLine(line).substring(0, start);
		    	
		    	var escapeFiltered = beforeCursor.replace("\\\\", "").replace("\\@", "");
		    	if ((escapeFiltered.match(/@/g) || []).length % 2 == 1) { // only show hint when type left @
		    		function hint(cm, showHintCallback) {
				    	var end = cm.getCursor().ch;
				    	var matchWith = cm.doc.getLine(line).substring(start, end);
				    	$input.data("showHintCallback", showHintCallback);
				    	varQueryCallback(matchWith, line, start, end);
					}
		    		hint.async = true;
					CodeMirror.showHint(cm, hint, {completeSingle: false});
		    	}
			}
		});
		
	    onedev.server.codeSupport.trackWidth(inputId);
    },
    showVariables: function(inputId, variables, line, start, end) {
    	$("#" + inputId).data("showHintCallback")({
    		list: variables,
    		from: CodeMirror.Pos(line, start-1),
    		to: CodeMirror.Pos(line, end)
    	});
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

        onedev.server.codeSupport.trackWidth(inputId);
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
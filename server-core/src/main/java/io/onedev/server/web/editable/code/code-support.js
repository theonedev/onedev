onedev.server.codeSupport = {
	adjustHeight: function($input) {
		var maxHeight = $input.parent().data("maxHeight");
		var minHeight = $input.parent().data("minHeight");
		
		var height = $input.next().find(".CodeMirror-sizer").outerHeight();
		
		if (height > maxHeight)
			height = maxHeight;
		if (height < minHeight)
			height = minHeight;
		$input.parent().height(height + 20);
	},
	onEditorLoad: function(inputId, modeName, varQueryCallback) {
		let cm = CodeMirror.fromTextArea(document.getElementById(inputId), {
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
		
		let $input = $("#" + inputId);
		$input.parent().data("minHeight", parseInt($input.parent().css("min-height"), 10));
		$input.parent().data("maxHeight", parseInt($input.parent().css("max-height"), 10));
		
		cm.on("change", function() {
			cm.save();
			onedev.server.form.markDirty($input.closest("form"));
			setTimeout(function() {
				onedev.server.codeSupport.adjustHeight($input);				
			}, 0);
		});
		
		onedev.server.codeSupport.adjustHeight($input);
		
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
		onedev.server.codeSupport.adjustHeight($("#" + inputId));
	}
}

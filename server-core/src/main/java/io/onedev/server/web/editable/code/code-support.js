onedev.server.codeSupport = {
	adjustHeight: function($input) {
		var maxHeight = $input.parent().data("maxHeight");
		var minHeight = $input.parent().data("minHeight");
		
		var height = $input.next().find(".CodeMirror-sizer").outerHeight();
		
		if (height > maxHeight)
			height = maxHeight;
		if (height < minHeight)
			height = minHeight;
		$input.parent().height(height);
	},
	onEditorLoad: function(inputId, modeName, varQueryCallback) {
		let cm = CodeMirror.fromTextArea(document.getElementById(inputId), {
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
		});
		
		onedev.server.codeSupport.adjustHeight($input);
		cm.refresh();
			
		function getLineBeforeCursor(cm) {
	    	var cursor = cm.getCursor();
	    	var line = cursor.line;
	    	return cm.doc.getLine(line).substring(0, cursor.ch);
		}
		cm.on("cursorActivity", function(cm, event) {
			setTimeout(function() {
		    	var lineBeforeCursor = getLineBeforeCursor(cm);
		    	if ($(".CodeMirror-hints").length == 0 && $(lineBeforeCursor.replace("@@", "").match(/@/g) || []).length % 2 == 1) { 
		    		function hint(cm, showHintCallback) {
				    	$input.data("showHintCallback", showHintCallback);
				    	var lineBeforeCursor = getLineBeforeCursor(cm);
						var lastIndex = lineBeforeCursor.lastIndexOf('@') + 1;
						var matchWith = lineBeforeCursor.substring(lastIndex, cm.getCursor().ch);
				    	varQueryCallback(matchWith, cm.getCursor().line, lastIndex);
					}
		    		hint.async = true;
					var extraKeys = {
						Esc: function(cm, handle) {
							// Close in a timeout so modal can check existance of 
							// hints to determine if Esc should be handled
							setTimeout(function() {handle.close();}, 0);
						}
					};
					CodeMirror.showHint(cm, hint, {completeSingle: false, closeCharacters: /@/, extraKeys: extraKeys});
		    	}
			}, 0);
		});
		
		$input.on("beforeDelete", function() {
			cm.toTextArea();
		});
    },
    showVariables: function(inputId, variables, line, start) {
    	var $input = $("#" + inputId);
		var cm = $input.next(".CodeMirror")[0].CodeMirror;		
		for (var i = 0; i < variables.length; i++) {
			variables[i].render = function(element, self, data) {
				if (data.description) {
					var $hint = $(document.createElement("div"));
					$hint.addClass("d-flex flex-nowrap justify-content-between");
					var $text = $(document.createElement("div"));
					$text.text(data.text);
					$hint.append($text);
					var $description = $(document.createElement("div"));
					$description.addClass("text-muted ml-4");
					$description.text(data.description);
					$hint.append($description);
					element.append($hint[0]);
				} else {
					element.append(data.text);
				}				
			}
		}		
		
	    var afterCursor = cm.doc.getLine(line).substring(start);
		var to = afterCursor.indexOf('@');
		if (to == -1)
			to = cm.getCursor().ch;
		else
			to += start+1;
		
    	$input.data("showHintCallback")({
    		list: variables,
    		from: CodeMirror.Pos(line, start-1),
    		to: CodeMirror.Pos(line, to)
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
			lineWrapping: true,
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
		cm.refresh();
	}
}

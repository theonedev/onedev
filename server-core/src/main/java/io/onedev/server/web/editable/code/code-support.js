onedev.server.codeSupport = {
	onEditorDomReady: function(inputId, modeName, variables, varMark) {
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
			if (String.fromCharCode(event.keyCode) == varMark) {
		    	var cursor = cm.getCursor();
		    	var line = cursor.line;
		    	var start = cursor.ch;

		    	if (start < 1 || cm.doc.getLine(line).substr(start-1, 1) != "\\") {
					CodeMirror.showHint(cm, function () {
				    	var end = cm.getCursor().ch;
				    	var matchWith = cm.doc.getLine(line).substr(start, end);
				    	var matched = variables.filter(function (item) {
				    		return item.text.indexOf(matchWith) >= 0;
				    	});
						for (var i = 0; i < matched.length; i++) {
							if (i == matched.length-1) {
								matched[i].render = function(element, self, data) {
									var $element = $(element);
									$element.append(data.text);
									$element.parent().append(
											"<li class='tips'>" +
											"<div>Type '" + varMark + "' to start inserting variable</div>" +
											"<div>Type '\\" + varMark + "' to input normal '" + varMark + "' character</div>" +
											"</li>");
								};
							} else {
								matched[i].render = function(element, self, data) {
									$(element).append(data.text);
								};
							}
						}
				    	return {
				    		list: matched,
				    		from: CodeMirror.Pos(line, start),
				    		to: CodeMirror.Pos(line, end)
				    	};
					}, {completeSingle: false});
		    	}
			}
		});
		
	    onedev.server.codeSupport.trackWidth(inputId);
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
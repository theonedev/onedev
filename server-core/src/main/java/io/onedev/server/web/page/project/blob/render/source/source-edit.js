onedev.server.sourceEdit = {
	onDomReady: function(containerId, filePath, mark, indentType, tabSize, lineWrapMode, 
			autoFocus, autosaveKey) {
		var $container = $("#" + containerId);
		var $sourceEdit = $container.children(".source-edit");
		var $warning = $sourceEdit.children(".warning");
		var $code = $sourceEdit.children(".code");
		var cm = CodeMirror($code[0], {
			value: $sourceEdit.children(".input").val(),
			autofocus: autoFocus,
			theme: "eclipse",
			indentWithTabs: indentType == "Tabs",
			indentUnit: tabSize,
			lineNumbers: true,
			lineWrapping: lineWrapMode == "Soft wrap",
			tabSize: tabSize,
			styleActiveLine: true,
			styleSelectedText: true,
			foldGutter: true,
			matchBrackets: true,
			scrollbarStyle: "simple",
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
			highlightIdentifiers: {delay: 500}
		});
		
		onedev.server.codemirror.setModeByFileName(cm, filePath);
		
		/*
		 * AreYouSure can not track dirty correctly for CodeMirror generated
		 * textarea, and it will always mark the form as clean even if we 
		 * mark it as dirty explicitly. So we use no-dirtytrack class to 
		 * disable automatic dirty track
		 */
		$sourceEdit.closest("form").find("textarea").addClass("no-dirtytrack").addClass("no-autosize");
		
		if (mark)
			onedev.server.codemirror.mark(cm, mark);
		
		cm.oldDocValue = cm.doc.getValue();

		onedev.server.codemirror.bindShortcuts(cm);
		
    	var doneTimer;
		cm.on("change", function() {
			$sourceEdit.closest("form").addClass("dirty");
			if (doneTimer) 
				clearTimeout(doneTimer);
			doneTimer = setTimeout(function() {
				var cm = $(".source-edit>.code>.CodeMirror")[0].CodeMirror;
				var content = cm.doc.getValue();
				if (content.trim().length != 0)
					localStorage.setItem(autosaveKey, content);
				cm = null;
			}, 500);
		});
		
		$code.on("getViewState", function(e) {
			return onedev.server.codemirror.getViewState(cm);
	    });
		$code.on("setViewState", function(e, viewState) {
			onedev.server.codemirror.setViewState(cm, viewState);
	    });
	    
		$code.on("resized", function() {
			setTimeout(function() {
				cm.setSize("100%", $code.height());
			});
			return false;
        });
        
        $sourceEdit.on("beforeSubmit", function() {
			var cm = $(".source-edit>.code>.CodeMirror")[0].CodeMirror;
            $sourceEdit.children("textarea").val(cm.getValue());
        });

		$warning.on("closed.bs.alert", function () {
			$(window).resize();
		});
	},
	onWindowLoad: function(containerId, mark, autosaveKey) {
		var $container = $("#" + containerId);
		var $warning = $container.find(">.source-edit>.warning");
		var cm = $(".source-edit>.code>.CodeMirror")[0].CodeMirror;
		onedev.server.form.registerAutosaveKey($container.closest("form.leave-confirm"), autosaveKey);
		var autosaveValue = localStorage.getItem(autosaveKey);
		if (autosaveValue) {
			cm.doc.setValue(autosaveValue);
			$warning.show();	
		}
		
		if (mark && onedev.server.viewState.getFromHistory() === undefined 
				&& onedev.server.viewState.carryOver === undefined) {
			onedev.server.codemirror.scrollTo(cm, mark);					
		}
	},
	mark: function(mark) {
		cm = $(".source-edit>.code>.CodeMirror")[0].CodeMirror;
		if (mark) {
			if (cm.oldDocValue) {
				var dmp = new diff_match_patch();
				var diffs = dmp.diff_main(cm.oldDocValue, cm.doc.getValue());
				
				var beginLine = mark.fromRow, beginChar = mark.fromColumn;
				var endLine = mark.toRow, endChar = mark.toColumn;
				var newBeginLine, newBeginChar, newEndLine, newEndChar;
				var oldLine = oldChar = newLine = newChar = 0;
				for (var i=0; i<diffs.length; i++) {
					var diff = diffs[i];
					var chars = diff[1];
					var quit = false;
					for (var j=0; j<chars.length; j++) {
						var char = chars[j];
						if (diff[0] == -1) {
							if (char == '\n') {
								oldLine++;
								oldChar = 0;
							} else {
								oldChar++;
							}
						} else if (diff[0] == 1) {
							if (char == '\n') {
								newLine++;
								newChar = 0;
							} else {
								newChar++;
							}
						} else {
							if (oldLine == beginLine && oldChar == beginChar) {
								newBeginLine = newLine;
								newBeginChar = newChar;
							} 
							if (char == '\n') {
								oldLine++;
								oldChar = 0;
								newLine++;
								newChar = 0;
							} else {
								oldChar++;
								newChar++;
							}
							if (oldLine == endLine && oldChar == endChar) {
								newEndLine = newLine;
								newEndChar = newChar;
							} 
						}
						if (oldLine>beginLine && newBeginLine==undefined || oldLine>endLine) {
							quit = true;
							break;
						}
					}
					if (quit)
						break;
				}
			}
			
			if (newBeginLine!=undefined && newEndLine!=undefined) {
				var newMark = {
					fromRow: newBeginLine, 
					fromColumn: newBeginChar, 
					toRow: newEndLine, 
					toColumn: newEndChar
				};
				onedev.server.codemirror.mark(cm, newMark);
				onedev.server.codemirror.scrollTo(cm, newMark);
			}			
		} else {
			onedev.server.codemirror.clearMark(cm);
		}
	},
	onIndentTypeChange: function(indentType) {
		var cm = $(".source-edit>.code>.CodeMirror")[0].CodeMirror;
		cm.setOption("indentWithTabs", indentType == "Tabs");
	},
	onLineWrapModeChange: function(lineWrapMode) {
		var cm = $(".source-edit>.code>.CodeMirror")[0].CodeMirror;
		cm.setOption("lineWrapping", lineWrapMode == "Soft wrap");
	},
	onTabSizeChange: function(tabSize) {
		var cm = $(".source-edit>.code>.CodeMirror")[0].CodeMirror;
		cm.setOption("tabSize", tabSize);
	}
};

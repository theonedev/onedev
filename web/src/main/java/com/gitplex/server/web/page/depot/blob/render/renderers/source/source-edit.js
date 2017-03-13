gitplex.server.sourceEdit = {
	onDomReady: function(containerId, filePath, mark, indentType, tabSize, lineWrapMode, autoFocus) {
		var $container = $("#" + containerId);
		var $sourceEdit = $container.children(".source-edit");
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
		
		gitplex.server.codemirror.setMode(cm, filePath);
		
		/*
		 * AreYouSure can not track dirty correctly for CodeMirror generated
		 * textarea, and it will always mark the form as clean even if we 
		 * mark it as dirty explicitly. So we use no-dirtycheck class to 
		 * disable automatic dirty track
		 */
		$sourceEdit.closest("form").find("textarea").addClass("no-dirtytrack");
		
		if (mark)
			gitplex.server.codemirror.mark(cm, mark);
		
		cm.oldDocValue = cm.doc.getValue();

		gitplex.server.codemirror.bindKeys(cm);
		
		function hideParentScrollBar() {
			var $scrollable = $sourceEdit.closest(".scrollable");
			/*
			 * Hide scroll bar of parent scrollable as we will be using 
			 * CodeMirror scroll bar
			 */
			$scrollable.css("overflow", "visible");
		}
		
		hideParentScrollBar();
		
		cm.on("change", function() {
			$sourceEdit.closest("form").addClass("dirty");
		});
		
		$sourceEdit.on("getViewState", function(e) {
			return gitplex.server.codemirror.getViewState(cm);
	    });
		$sourceEdit.on("setViewState", function(e, viewState) {
			gitplex.server.codemirror.setViewState(cm, viewState);
	    });
	    
		$sourceEdit.on("autofit", function(e, width, height) {
			if (cm.getOption("fullScreen"))
				return;
			
			$sourceEdit.outerWidth(width);
			$sourceEdit.outerHeight(height);
			if (cm.getOption("fullScreen"))
				cm.setOption("fullScreen", false);
			cm.setSize($sourceEdit.width(), $sourceEdit.height());
		});
		
		$sourceEdit.on("show", hideParentScrollBar);
	},
	onWindowLoad: function(containerId, mark) {
		var cm = gitplex.server.sourceEdit.getCodeMirror(containerId);
		if (mark && gitplex.server.viewState.getFromHistory() === undefined 
				&& gitplex.server.viewState.getFromCarryOver() === undefined) {
			gitplex.server.codemirror.scrollTo(cm, mark);					
		}
	},
	mark: function(containerId, mark) {
		var cm = gitplex.server.sourceEdit.getCodeMirror(containerId);		
		if (mark) {
			if (cm.oldDocValue) {
				var dmp = new diff_match_patch();
				var diffs = dmp.diff_main(cm.oldDocValue, cm.doc.getValue());
				
				var beginLine = mark.beginLine, beginChar = mark.beginChar;
				var endLine = mark.endLine, endChar = mark.endChar;
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
					beginLine: newBeginLine, 
					beginChar: newBeginChar, 
					endLine: newEndLine, 
					endChar: newEndChar
				};
				gitplex.server.codemirror.mark(cm, newMark);
				gitplex.server.codemirror.scrollTo(cm, newMark);
			}			
		} else {
			gitplex.server.codemirror.clearMark(cm);
		}
	},
	getCodeMirror: function(containerId) {
		return $("#" + containerId + ">.source-edit>.code>.CodeMirror")[0].CodeMirror;		
	},
	onSubmit: function(containerId) {
		var cm = gitplex.server.sourceEdit.getCodeMirror(containerId);		
		$("#" + containerId + ">.source-edit>textarea").val(cm.getValue());
	},
	onIndentTypeChange: function(containerId, indentType) {
		var cm = gitplex.server.sourceEdit.getCodeMirror(containerId);		
		cm.setOption("indentWithTabs", indentType == "Tabs");
	},
	onLineWrapModeChange: function(containerId, lineWrapMode) {
		var cm = gitplex.server.sourceEdit.getCodeMirror(containerId);		
		cm.setOption("lineWrapping", lineWrapMode == "Soft wrap");
	},
	onTabSizeChange: function(containerId, tabSize) {
		var cm = gitplex.server.sourceEdit.getCodeMirror(containerId);		
		cm.setOption("tabSize", tabSize);
	}
};

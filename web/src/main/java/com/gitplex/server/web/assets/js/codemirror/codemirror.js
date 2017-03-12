gitplex.server.codemirror = {
	clearMark: function(cm) {
		var marks = cm.getAllMarks();
		for (var i=0; i<marks.length; i++)  {
			marks[i].clear();
		}
	},
	clearSelection: function(cm) {
    	cm.setCursor(cm.getCursor("from"));
	},
	mark: function(cm, mark) {
		gitplex.server.codemirror.clearMark(cm);
		cm.markText(
				{line: mark.beginLine, ch: mark.beginChar}, 
				{line: mark.endLine, ch: mark.endChar},
				{className: "CodeMirror-mark"});
	},
	scrollTo: function(cm, mark) {
		var top = cm.charCoords({line: mark.beginLine, ch: 0}, "local").top;
		var bottom = cm.charCoords({line: mark.endLine, ch: 0}, "local").bottom;
		
		var markHeight = bottom - top;
		var clientHeight = cm.getScrollInfo().clientHeight;
		if (clientHeight <= markHeight) {
			cm.scrollTo(null, top - 50); 			
		} else {
			cm.scrollTo(null, (top+bottom-clientHeight)/2); 			
		}
	},
	setMode: function(cm, filePath) {
	    var modeInfo = CodeMirror.findModeByFileName(filePath);
	    if (modeInfo) {
	    	// specify mode via mime does not work for gfm (github flavored markdown)
	    	if (modeInfo.mode === "gfm")
	    		cm.setOption("mode", "gfm");
	    	else
	    		cm.setOption("mode", modeInfo.mime);
			CodeMirror.autoLoadMode(cm, modeInfo.mode);
	    }
	},
	getViewState: function(cm) {
		var cursor = cm.getCursor();
    	var scrollInfo = cm.getScrollInfo();
    	var scroll = {left: scrollInfo.left, top: scrollInfo.top};
		return {cursor: cursor, scroll: scroll};		
	},
	setViewState: function(cm, viewState) {
    	if (viewState.cursor) {
    		cm.setCursor(viewState.cursor);
    		if (!viewState.scroll) {
    			var h = cm.getScrollInfo().clientHeight;
    			var coords = cm.charCoords({line: viewState.cursor.line, ch: 0}, "local");
    			cm.scrollTo(null, (coords.top + coords.bottom - h) / 2); 			
    		}
    	}
    	if (viewState.scroll) {
	    	cm.scrollTo(viewState.scroll.left, viewState.scroll.top);
    	}
	}, 
	bindKeys: function() {
		if (!($(document).data("CodeMirrorKeyBinded"))) {
			$(document).data("CodeMirrorKeyBinded", true);
			
			function find() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("find");
					return false;
				}
			}
			function findNext() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("findNext");
					return false;
				}
			}
			function findPrev() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("findPrev");
					return false;
				}
			}
			function gotoLine() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("gotoLine");
					return false;
				}
			}
			function goDocStart() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("goDocStart");
					return false;
				}
			}
			function goDocEnd() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("goDocEnd");
					return false;
				}
			}
			function goPageUp() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("goPageUp");
					return false;
				}
			}
			function goPageDown() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("goPageDown");
					return false;
				}
			}
			function goLineUp() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("goLineUp");
					return false;
				}
			}
			function goLineDown() {
				if ($(".code>.CodeMirror").length != 0) {
					$(".code>.CodeMirror")[0].CodeMirror.execCommand("goLineDown");
					return false;
				}
			}
			if (gitplex.server.isMac()) {
				$(document).keydown(function(e) {
					if (e.metaKey && !e.ctrlKey && !e.altKey) {
						if (!e.shiftKey && e.keyCode == 70) 
							return find();
						else if (e.shiftKey && e.keyCode == 71)
							return findPrev();
						else if (!e.shiftKey && e.keyCode == 71)
							return findNext();
						else if (!e.shiftKey && e.keyCode == 38)
							return goDocStart();
						else if (!e.shiftKey && e.keyCode == 40)
							return goDocEnd();
					}
				});
			} else {
				$(document).bind("keydown", "Ctrl+f", find);
				$(document).bind("keydown", "Ctrl+g", findNext);
				$(document).bind("keydown", "Ctrl+Shift+g", findPrev);
				$(document).bind("keydown", "Ctrl+home", goDocStart);
				$(document).bind("keydown", "Ctrl+end", goDocEnd);
			}
			$(document).bind("keydown", "Ctrl+g", gotoLine);
			$(document).bind("keydown", "pageup", goPageUp);
			$(document).bind("keydown", "pagedown", goPageDown);
			$(document).bind("keydown", "up", goLineUp);
			$(document).bind("keydown", "down", goLineDown);
		}
	}
};
onedev.server.codemirror = {
	clearMark: function(cm) {
		var marks = cm.getAllMarks();
		for (var i=0; i<marks.length; i++)  {
			marks[i].clear();
		}
	},
	clearSelection: function(cm) {
    	cm.setCursor(cm.getCursor("from"));
	},
	mark: function(cm, range) {
        onedev.server.codemirror.clearMark(cm);
		if (Array.isArray(range)) {
			for (var i in range) {
				cm.markText(
						{line: range[i].fromRow, ch: range[i].fromColumn}, 
						{line: range[i].toRow, ch: range[i].toColumn},
						{className: "CodeMirror-mark"});
			}			
		} else {
			cm.markText(
					{line: range.fromRow, ch: range.fromColumn}, 
					{line: range.toRow, ch: range.toColumn},
					{className: "CodeMirror-mark"});
		}
	},
	scrollTo: function(cm, range) {
		var top = cm.charCoords({line: range.fromRow, ch: 0}, "local").top;
		cm.scrollTo(null, top - 50); 			
	},
	scrollIntoView: function(cm, range) {
		cm.scrollIntoView({line: range.fromRow, ch: 0}, 8);
	},
	setModeByName: function(cm, modeName) {
	    var modeInfo = CodeMirror.findModeByName(modeName);
        if (modeInfo) 
            onedev.server.codemirror.setMode(cm, modeInfo);
	},
	setModeByFileName: function(cm, fileName) {
		if (fileName.endsWith(".cbl") || fileName.endsWith(".pco")) {
			onedev.server.codemirror.setModeByName(cm, "cobol");
		} else if (fileName.endsWith(".js")) {
			onedev.server.codemirror.setModeByName(cm, "jsx");
		} else if (fileName == ".onedev-buildspec") {
			onedev.server.codemirror.setModeByName(cm, "xml");
		} else {
		    var modeInfo = CodeMirror.findModeByFileName(fileName);
	        if (modeInfo) 
	            onedev.server.codemirror.setMode(cm, modeInfo);
		}
	},
	setMode: function(cm, modeInfo) {
        // specify mode via mime does not work for gfm (github flavored markdown)
        if (modeInfo.mode === "gfm")
            cm.setOption("mode", "gfm");
        else
            cm.setOption("mode", modeInfo.mime);
        CodeMirror.autoLoadMode(cm, modeInfo.mode);
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
	bindShortcuts: function(cm) {
		cm.setOption("extraKeys", {
			"F11": function(cm) {
				cm.setOption("fullScreen", !cm.getOption("fullScreen"));
			},
			"Esc": function(cm) {
				if (cm.getOption("fullScreen")) {
					cm.setOption("fullScreen", false);
					$(window).resize();
				}
	        }
		});
		if (onedev.server.util.isMac()) {
		    CodeMirror.keyMap.default["Cmd-L"] = "gotoLine";
		} else {
		    CodeMirror.keyMap.default["Ctrl-L"] = "gotoLine";
		}
		
		if (!($(document).data("CodeMirrorShortcutsBinded"))) {
			$(document).data("CodeMirrorShortcutsBinded", true);
			
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
			if (onedev.server.util.isMac()) {
				$(document).bind("keydown", "Meta+f", find);
				$(document).bind("keydown", "Meta+g", findNext);
				$(document).bind("keydown", "Meta+Shift+g", findPrev);
				$(document).bind("keydown", "Meta+up", goDocStart);
				$(document).bind("keydown", "Meta+down", goDocEnd);
				$(document).bind("keydown", "Meta+l", gotoLine);
			} else {
				$(document).bind("keydown", "Ctrl+f", find);
				$(document).bind("keydown", "Ctrl+g", findNext);
				$(document).bind("keydown", "Ctrl+Shift+g", findPrev);
				$(document).bind("keydown", "Ctrl+home", goDocStart);
				$(document).bind("keydown", "Ctrl+end", goDocEnd);
				$(document).bind("keydown", "Ctrl+l", gotoLine);
			}
			$(document).bind("keydown", "pageup", goPageUp);
			$(document).bind("keydown", "pagedown", goPageDown);
			$(document).bind("keydown", "up", goLineUp);
			$(document).bind("keydown", "down", goLineDown);
		}
	}
};

$(document).on("beforeElementReplace", function(event, componentId) {
	var $component = $("#" + componentId);
	$component.find(".CodeMirror").each(function() {
		var cm = this.CodeMirror;
		if (cm.toTextArea)
			cm.toTextArea();
	}).remove();
	$component.find("form").addBack("form").each(function() {
		this.submit = undefined;
	});
});

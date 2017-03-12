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
	} 
};
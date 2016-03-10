pmease.commons.codemirror = {
	centerLine: function(cm, line) {
		var h = cm.getScrollInfo().clientHeight;
		var coords = cm.charCoords({line: line, ch: 0}, "local");
		cm.scrollTo(null, (coords.top + coords.bottom - h) / 2); 			
	},
	mark: function(cm, mark) {
		pmease.commons.codemirror.centerLine(cm, mark.beginLine);
		
		var allMarks = cm.getAllMarks();
		for (var i=0; i<allMarks.length; i++) 
			allMarks[i].clear();
		cm.markText(
				{line: mark.beginLine, ch: mark.beginChar}, 
				{line: mark.endLine, ch: mark.endChar},
				{className: "CodeMirror-mark"});
		cm.setCursor({line:mark.beginLine, ch:mark.beginChar});
		setTimeout(function() {
			cm.focus();
		}, 10);
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
	initState: function(cm, viewState) {
	    if (viewState) {
	    	pmease.commons.history.setViewState(viewState);
	    }

	    // use timer to minimize performance impact 
	    var timer;
	    function onViewStateChange() {
    		if (timer)
    			clearTimeout(timer);
    		timer = setTimeout(function() {
    			timer = undefined;
    			var cursor = cm.getCursor();
    	    	var scrollInfo = cm.getScrollInfo();
    	    	var scroll = {left: scrollInfo.left, top: scrollInfo.top};
    	    	pmease.commons.history.setViewState({cursor: cursor, scroll: scroll});
	    	}, 500);
	    };
	    
	    cm.on("cursorActivity", onViewStateChange);
	    cm.on("scroll", onViewStateChange);

	    var viewState = pmease.commons.history.getViewState();
	    if (viewState) {
	    	if (viewState.cursor)
	    		cm.setCursor(viewState.cursor);
	    	if (viewState.scroll)
	    		cm.scrollTo(viewState.scroll.left, viewState.scroll.top);
	    }
	    
	    CodeMirror.keyMap.default["Ctrl-L"] = "gotoLine";
	    cm.focus();
	} 
};
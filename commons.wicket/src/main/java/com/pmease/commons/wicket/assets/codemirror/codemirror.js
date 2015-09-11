gitplex.codemirror = {
	initState: function(cm, stateStr) {
	    // use timer to minimize performance impact 
	    var cursorTimer;
	    cm.on("cursorActivity", function() {
    		if (cursorTimer)
    			clearTimeout(cursorTimer);
    		cursorTimer = setTimeout(function() {
    			cursorTimer = undefined;
    			var cursor = cm.getCursor();
		    	pmease.commons.history.setCursor(cursor);
		    	$(".preserve-cm-state").each(function() {
		    		var state = $(this).data("state");
		    		if (!state)
		    			state = {};
		    		state.cursor = cursor;
		    		$(this).data("state", state);
		    		var uri = new URI(this);
		    		uri.removeSearch("state");
		    		uri.addSearch("state", JSON.stringify(state));
		    		
		    	});
	    	}, 500);
	    });
	    
	    var cursor = pmease.commons.history.getCursor();
	    if (cursor)
	    	cm.setCursor(cursor);
	    
	    // use timer to minimize performance impact 
	    var scrollTimer;
	    cm.on("scroll", function() {
	    	gitplex.mouseState.moved = false;			    	
	    	if (scrollTimer)
	    		clearTimeout(scrollTimer);
	    	scrollTimer = setTimeout(function() {
	    		scrollTimer = undefined;
		    	var scrollInfo = cm.getScrollInfo();
		    	var scroll = {left: scrollInfo.left, top: scrollInfo.top};
		    	console.log(scroll.top);
		    	pmease.commons.history.setScroll(scroll);
		    	$(".preserve-cm-state").each(function() {
		    		var state = $(this).data("state");
		    		if (!state)
		    			state = {};
		    		state.scroll = scroll;
		    		$(this).data("state", state);
		    	});
	    	}, 500);
	    });
	    var scroll = pmease.commons.history.getScroll();
	    if (scroll)
	    	cm.scrollTo(scroll.left, scroll.top);
	    
	    if (stateStr) {
	    	var state = JSON.parse(stateStr);
	    	if (state.cursor)
	    		cm.setCursor(state.cursor);
	    	if (state.scroll) 
	    		cm.scrollTo(state.scroll.left, state.scroll.top);
	    }
	    
	}		
};
gitplex.codemirror = {
	initState: function(cm, clientStateStr) {
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
		    		var clientState = $(this).data("client_state");
		    		if (!clientState)
		    			clientState = {};
		    		clientState.cursor = cursor;
		    		$(this).data("client_state", clientState);
		    		
		    		var uri = new URI(this);
		    		uri.removeSearch("client_state");
		    		uri.addSearch("client_state", JSON.stringify(clientState));
		    		$(this).attr("href", uri.href());
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
		    	pmease.commons.history.setScroll(scroll);
		    	$(".preserve-cm-state").each(function() {
		    		var clientState = $(this).data("client_state");
		    		if (!clientState)
		    			clientState = {};
		    		clientState.scroll = scroll;
		    		$(this).data("client_state", clientState);
		    		
		    		var uri = new URI(this);
		    		uri.removeSearch("client_state");
		    		uri.addSearch("client_state", JSON.stringify(clientState));
		    		$(this).attr("href", uri.href());
		    	});
	    	}, 500);
	    });
	    var scroll = pmease.commons.history.getScroll();
	    if (scroll)
	    	cm.scrollTo(scroll.left, scroll.top);
	    
	    if (clientStateStr) {
	    	var clientState = JSON.parse(clientStateStr);
	    	if (clientState.cursor)
	    		cm.setCursor(clientState.cursor);
	    	if (clientState.scroll) 
	    		cm.scrollTo(clientState.scroll.left, clientState.scroll.top);
	    }
	    
	}		
};
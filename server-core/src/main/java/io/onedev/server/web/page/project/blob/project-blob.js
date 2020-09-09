onedev.server.projectBlob = {
	onDomReady: function(callback) {
		var cookieKey = "projectBlob.searchResult.height";
		
		var $searchResult = $(".project-blob>.search-result");
		var height = Cookies.get(cookieKey);
		if (height) 
			$searchResult.outerHeight(height);
		
		$searchResult.resizable({
			autoHide: false,
			handles: {"n": "#search-result-resize-handle"},
			minHeight: 100,
			resize: function(e, ui) {
				var blobContentHeight = $(".project-blob>.blob-content").outerHeight();
			    if(blobContentHeight < 150)
			    	$(this).resizable({maxHeight: ui.size.height});
			},
			stop: function(e, ui) {
				$(this).resizable({maxHeight: undefined});
				Cookies.set(cookieKey, ui.size.height, {expires: Infinity});
			}
		});
					
		/*
		 * Do not use hotkey plugin here as otherwise codemirror search will not function 
		 * properly in readonly mode
		 */
		$(document).on("keydown", function(e) {
			if ($(".modal:visible").length == 0 && !onedev.server.util.canInput(e.target)) {
				if (e.keyCode == 84) { // 't'
					callback("quickSearch");
				} else if (e.keyCode == 86) { // 'v'
					callback("advancedSearch");
				} else if (e.keyCode == 89) { // 'y'
					callback("permalink");
				}
			}
		});
	} 
}

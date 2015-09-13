$(window).load(function() {
	var cookieKey = "repoFile.searchResult.height";
	$("body").css("overflow", "hidden");
	var $searchResult = $("#repo-file>.search-result");
	var height = Cookies.get(cookieKey);
	if (height) 
		$searchResult.outerHeight(height);
	$searchResult.resizable({
		autoHide: false,
		handles: {"n": "#search-result-resize-handle"},
		minHeight: 75,
		resize: function(e, ui) {
			var $fileViewer = $("#repo-file>.file-viewer");
			var fileViewerHeight = $fileViewer.outerHeight();
		    if(fileViewerHeight < 100)
		    	$(this).resizable({maxHeight: ui.size.height});
		},
		stop: function(e, ui) {
			$(this).resizable({maxHeight: undefined});
			Cookies.set(cookieKey, ui.size.height, {expires: Infinity});
		}
	});
	
	$(window).resize(function(e) {
		e.stopPropagation();
		
		var $head = $("#repo-file>.head");
		var $revisionSelector = $head.find(">.revision-selector");
				
		// we should simply call $head.width() here, but the value is incorrect after maximize and restore
		// window in IE and Chrome (maybe due to use of table in sidebar?), so we go with the complicate 
		// approach of calculating the head width
		var headWidth = $("#repository").width() - $("#repository>.body>.sidebar>table>tbody>tr>td.nav").outerWidth();
		
		// below code moves file navigator to bottom if it is too wide
		var maxWidth = headWidth - $revisionSelector.outerWidth() - 60;
		var maxHeight = $head.height();

		var $fileNavigator = $head.find(">.file-navigator");
		if ($fileNavigator.length != 0) {
			if ($fileNavigator.outerWidth() > maxWidth || $fileNavigator.outerHeight() > maxHeight)
				$("#repo-file>.file-navigator").show().append($fileNavigator);
		} else {
			$fileNavigator = $("#repo-file>.file-navigator>div");
			if ($fileNavigator.outerWidth() <= maxWidth && $fileNavigator.outerHeight() <= maxHeight) {
				$fileNavigator.insertAfter($revisionSelector);
				$("#repo-file>.file-navigator").hide();
			}
		}
		
		// below code moves last commit message to bottom if it is too wide
		var $lastCommit = $("#repo-file>.last-commit");
		if ($lastCommit.length != 0) {
			// why calculate width this way, see above comment when handling file navigator 
			maxWidth = headWidth - ($lastCommit.outerWidth()-$lastCommit.width()) 
					- $lastCommit.find(".author").outerWidth(true) 
					- $lastCommit.find(".date").outerWidth(true)
					- $lastCommit.find(".hash").outerWidth(true);
			maxHeight = $lastCommit.find(".author").outerHeight();
			var $message = $lastCommit.find(".last-commit>span.message");
			if ($message.length != 0) {
				if ($message.outerWidth(true) > maxWidth || $message.outerHeight() > maxHeight)
					$lastCommit.find("div.message").show().append($message);
			} else {
				$message = $lastCommit.find("div.message>span");
				if ($message.outerWidth(true) <= maxWidth && $message.outerHeight() <= maxHeight) {
					$message.insertAfter($lastCommit.find(".date"));
					$lastCommit.find("div.message").hide();
				}
			}
		}
		
		var $fileViewer = $("#repo-file>.file-viewer");
		var width = $(window).width()-$("#repo-file").parent().prev().outerWidth();
		var height = $(window).height()-$fileViewer.offset().top;
		if ($("#main>.foot").is(":visible")) 
			height -= $("#main>.foot").outerHeight();
		if ($searchResult.is(":visible")) {
			$searchResult.outerWidth(width);
			var $searchResultBody = $searchResult.find(".search-result>.body");
			$searchResultBody.outerWidth($searchResult.width());
			height -= $searchResult.outerHeight();
			$searchResultBody.outerHeight($searchResult.height()
					-$searchResult.find(".search-result>.head").outerHeight()
					-$("#search-result-resize-handle").outerHeight());
		}
		$fileViewer.outerWidth(width).outerHeight(height);
		$fileViewer.closestDescendant(".autofit:visible").trigger("autofit", [$fileViewer.width(), $fileViewer.height()]);
	});
	$(window).resize();
});

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
		var maxWidth = $head.width() - $revisionSelector.outerWidth() - 60;
		var maxHeight = $head.height();
		
		var $fileNavigator = $head.find(">.file-navigator");
		if ($fileNavigator.length != 0) {
			if ($fileNavigator.outerWidth() > maxWidth || $fileNavigator.outerHeight() > maxHeight) {
				$("#repo-file>.file-navigator").show().append($fileNavigator);
				$fileNavigator.find("input.name")
						.css("border-top", "1px solid #BBB")
						.css("border-bottom", "1px solid #BBB")
						.css("height", "28px")
						.css("margin-top", "-2px")
						.css("border-radius", "3px");
			}
		} else {
			$fileNavigator = $("#repo-file>.file-navigator>div");
			if ($fileNavigator.outerWidth() <= maxWidth && $fileNavigator.outerHeight() <= maxHeight) {
				$fileNavigator.insertAfter($revisionSelector);
				$fileNavigator.find("input.name")
						.css("border-top-style", "none")
						.css("border-bottom-style", "none")
						.css("height", "30px")
						.css("margin-top", "-4px")
						.css("border-radius", "0");
				$("#repo-file>.file-navigator").hide();
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
		$fileViewer.closestDescendant(".autofit").trigger("autofit", [$fileViewer.width(), $fileViewer.height()]);
	});
	$(window).resize();
});
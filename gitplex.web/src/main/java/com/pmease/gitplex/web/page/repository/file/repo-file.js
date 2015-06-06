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
		event.stopPropagation();
		var $fileViewer = $("#repo-file>.file-viewer");
		var width = $(window).width()-$("#repo-file").parent().prev().outerWidth();
		var height = $(window).height()-$fileViewer.offset().top-$("#main>.foot").outerHeight();
		if ($searchResult.is(":visible")) {
			height -= $searchResult.outerHeight();
			$searchResult.find(">div>.body").outerHeight($searchResult.height()
					-$searchResult.find(">div>.head").outerHeight()
					-$("#search-result-resize-handle").outerHeight());
		}
		$fileViewer.outerWidth(width).outerHeight(height);
		$fileViewer.closestDescendant(".autofit").trigger("autofit", [$fileViewer.width(), $fileViewer.height()]);
	});
	$(window).resize();
});

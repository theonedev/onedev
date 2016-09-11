$(window).load(function() {
	if ($("#repo-file").length != 0) {
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
			var $revisionPicker = $head.find(">.revision-picker");
					
			// we should simply call $head.width() here, but the value is incorrect after maximize and restore
			// window in IE and Chrome (maybe due to use of table in sidebar?), so we go with the complicate 
			// approach of calculating the head width
			var headWidth = $("#depot").width() - $("#depot>.sidebar>table>tbody>tr>td.nav").outerWidth();
			
			// below code moves file navigator to bottom if it is too wide
			var maxWidth = headWidth - $revisionPicker.outerWidth() - 110;
			var maxHeight = $head.height();

			var $fileNavigator = $head.find(">.file-navigator");
			if ($fileNavigator.length != 0) {
				if ($fileNavigator.outerWidth() > maxWidth || $fileNavigator.outerHeight() > maxHeight)
					$("#repo-file>.file-navigator").show().append($fileNavigator);
			} else {
				$fileNavigator = $("#repo-file>.file-navigator>div");
				if ($fileNavigator.outerWidth() <= maxWidth && $fileNavigator.outerHeight() <= maxHeight) {
					$fileNavigator.insertAfter($revisionPicker);
					$("#repo-file>.file-navigator").hide();
				}
			}

			var $fileViewer = $("#repo-file>.file-viewer");
			var width = $(window).width()-$("#repo-file").parent().prev().outerWidth();
			var height = $(window).height()-$fileViewer.offset().top;
			if ($("#main>.foot").length != 0) 
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
			$fileViewer.find(".autofit:visible").first().trigger("autofit", [$fileViewer.width(), $fileViewer.height()]);
		});
		$(window).resize();
	}
});

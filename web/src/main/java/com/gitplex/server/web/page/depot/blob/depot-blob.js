$(window).load(function() {
	if ($("#depot-blob").length != 0) {
		var cookieKey = "repoFile.searchResult.height";
		$("body").css("overflow", "hidden");
		var $searchResult = $("#depot-blob>.search-result");
		var height = Cookies.get(cookieKey);
		if (height) 
			$searchResult.outerHeight(height);
		$searchResult.resizable({
			autoHide: false,
			handles: {"n": "#search-result-resize-handle"},
			minHeight: 75,
			resize: function(e, ui) {
				var $blobContent = $("#depot-blob>.blob-content");
				var blobContentHeight = $blobContent.outerHeight();
			    if(blobContentHeight < 100)
			    	$(this).resizable({maxHeight: ui.size.height});
			},
			stop: function(e, ui) {
				$(this).resizable({maxHeight: undefined});
				Cookies.set(cookieKey, ui.size.height, {expires: Infinity});
			}
		});
		
		if (location.hash) { // scroll anchors into view (for instance the markdown headline)
			setTimeout(function() {
				var element = document.getElementsByName(decodeURIComponent(location.hash.slice(1)))[0];
				if (element)
					element.scrollIntoView();
			}, 0);
		}
		
		$(window).resize(function(e) {
			e.stopPropagation();
			
			var $head = $("#depot-blob>.head");
			var $revisionPicker = $head.find(">.revision-picker");
					
			// we should simply call $head.width() here, but the value is incorrect after maximize and restore
			// window in IE and Chrome (maybe due to use of table in sidebar?), so we go with the complicate 
			// approach of calculating the head width
			var headWidth = $("#depot").width() - $("#depot>.sidebar>table>tbody>tr>td.nav").outerWidth();
			
			// below code moves file navigator to bottom if it is too wide
			var maxWidth = headWidth - $revisionPicker.outerWidth() - 110;
			var maxHeight = $head.height();

			var $blobNavigator = $head.find(">.blob-navigator");
			if ($blobNavigator.length != 0) {
				if ($blobNavigator.outerWidth() > maxWidth || $blobNavigator.outerHeight() > maxHeight)
					$("#depot-blob>.blob-navigator").show().append($blobNavigator);
			} else {
				$blobNavigator = $("#depot-blob>.blob-navigator>div");
				if ($blobNavigator.outerWidth() <= maxWidth && $blobNavigator.outerHeight() <= maxHeight) {
					$blobNavigator.insertAfter($revisionPicker);
					$("#depot-blob>.blob-navigator").hide();
				}
			}

			var $blobContent = $("#depot-blob>.blob-content");
			var width = $(window).width()-$("#depot-blob").parent().prev().outerWidth();
			var height = $(window).height()-$blobContent.offset().top;
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
			$blobContent.outerWidth(width).outerHeight(height-1);
			$blobContent.find(".autofit:visible").first().triggerHandler("autofit", [$blobContent.width(), $blobContent.height()]);
		});
		$(window).resize();
	}
});

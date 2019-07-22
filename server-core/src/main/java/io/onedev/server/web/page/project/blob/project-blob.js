onedev.server.projectBlob = {
	onDomReady: function(callback) {
		// hide browser scrollbar as we will manage it in blob content container 
		$("body").css("overflow", "hidden");
		
		var cookieKey = "projectBlob.searchResult.height";
		
		var $searchResult = $("#project-blob>.search-result");
		var height = Cookies.get(cookieKey);
		if (height) 
			$searchResult.outerHeight(height);
		
		$searchResult.resizable({
			autoHide: false,
			handles: {"n": "#search-result-resize-handle"},
			minHeight: 100,
			resize: function(e, ui) {
				var blobContentHeight = $("#project-blob>.blob-content").outerHeight();
			    if(blobContentHeight < 150)
			    	$(this).resizable({maxHeight: ui.size.height});
			},
			stop: function(e, ui) {
				$(this).resizable({maxHeight: undefined});
				Cookies.set(cookieKey, ui.size.height, {expires: Infinity});
			}
		});
		
		$(window).resize(function(e) {
			e.stopPropagation();

			var additionalWidth = 0;
			if ($(".source-view .CodeMirror-annotations").length != 0)
				additionalWidth ++;
			if ($(".source-view>.comment").length != 0)
				additionalWidth ++;
			if ($(".source-view>.outline").length != 0)
				additionalWidth ++;
			
			$("body").removeClass("additionalWidth1").removeClass("additionalWidth2").removeClass("additionalWidth3");
			if (additionalWidth != 0)
				$("body").addClass("additionalWidth" + additionalWidth);
			
			var $head = $("#project-blob>.head");
			var $revisionPicker = $head.find(">.revision-picker");
			var $operations = $head.find(">.operations");
					
			var headWidth = $head.width();

			// below code moves file navigator to bottom if it is too wide
			var maxWidth = headWidth - $revisionPicker.outerWidth() - $operations.outerWidth();
			var maxHeight = $head.height();

			var $blobNavigator = $head.find(">.blob-navigator");
			if ($blobNavigator.length != 0) {
				if ($blobNavigator.outerWidth() > maxWidth || $blobNavigator.outerHeight() > maxHeight)
					$("#project-blob>.blob-navigator").show().append($blobNavigator);
			} else {
				$blobNavigator = $("#project-blob>.blob-navigator>div");
				if ($blobNavigator.outerWidth() <= maxWidth && $blobNavigator.outerHeight() <= maxHeight) {
					$blobNavigator.insertAfter($revisionPicker);
					$("#project-blob>.blob-navigator").hide();
				}
			}

			var $blobContent = $("#project-blob>.blob-content");
			
			var minWindowWidth = onedev.server.projectBlob.parseCssDimension($("body").css("min-width"));

			var windowWidth = $(window).width();
			if (windowWidth < minWindowWidth) {
				windowWidth = minWindowWidth;
				$("body").css("overflow-x", "visible");
			} else {
				$("body").css("overflow-x", "hidden");
			}
			var width = windowWidth-$blobContent.offset().left;

			var height = $(window).height()-$blobContent.offset().top;
			
			var $searchResult = $("#project-blob>.search-result");
			if ($searchResult.is(":visible")) {
				var $searchResultHead = $searchResult.find(".search-result>.head");
				var $searchResultBody = $searchResult.find(".search-result>.body");
				
				$searchResult.outerWidth(width);
				$searchResultBody.outerWidth($searchResult.width());

				var searchResultBodyHeight = $searchResult.height() 
						- $("#search-result-resize-handle").outerHeight() 
						- $searchResultHead.outerHeight();
				$searchResultBody.outerHeight(searchResultBodyHeight);
				
				height -= $searchResult.outerHeight();
			}
			$blobContent.outerWidth(width).outerHeight(height);
			$blobContent.find(".autofit:visible").first().triggerHandler(
					"autofit", [$blobContent.width(), $blobContent.height()]);
		});

		/*
		 * Do not use hotkey plugin here as otherwise codemirror search will not function 
		 * properly in readonly mode
		 */
		$(document).on("keydown", function(e) {
			if ($(".modal:visible").length == 0 && !onedev.server.util.canInput(e.target)) {
				if (e.keyCode == 84) {
					callback("quickSearch");
				} else if (e.keyCode == 86) {
					callback("advancedSearch");
				}
			}
		});
		
	},
	parseCssDimension: function(cssDimension) {
		var index = cssDimension.indexOf("px");
		if (index && index != -1) {
			return parseInt(cssDimension.substring(0, index));
		} else {
			return 0;
		}
	},
	onWindowLoad: function() {
		if (location.hash && !onedev.server.viewState.getFromHistory()) {
			// Scroll anchors into view (for instance the markdown headline)
			var element = document.getElementsByName(decodeURIComponent(location.hash.slice(1)))[0];
			if (element)
				element.jumpIntoView();
		}
	}
}

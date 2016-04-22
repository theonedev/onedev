gitplex.textdiff = {
	init: function(containerId, symbolTooltipId, oldRev, newRev) {
		var $container = $("#" + containerId);
		var $symbols = $container.find(".cm-property, .cm-variable, .cm-variable-2, .cm-variable-3, .cm-def, .cm-meta"); 
		$symbols.mouseover(function() {
			var revision;
			var $symbol = $(this);
			if ($symbol.hasClass("delete")) {
				revision = oldRev;
			} else {
				var $td = $symbol.closest("td");
				
				// if is deleted line or if it is on left side of split view
				if ($td.hasClass("old") && !$td.hasClass("new") || $td.next().is("td"))
					revision = oldRev;
				else
					revision = newRev;
			}
			var symbolTooltip = document.getElementById(symbolTooltipId);
			if (symbolTooltip.onMouseOverSymbol)
				symbolTooltip.onMouseOverSymbol(revision, this);
		});
		$container.find("td.content").mouseover(function() {
			if (!gitplex.mouseState.pressed) {
				if ($(this).hasClass("left")) {
					$container.find("td.content.right").addClass("noselect");
					$container.find("td.content.left").removeClass("noselect");
				} else if ($(this).hasClass("right")) {
					$container.find("td.content.left").addClass("noselect");
					$container.find("td.content.right").removeClass("noselect");
				}
			}
		});
	    $container.on("mouseup keyup", function() {
	    	setTimeout(function() { // use a timeout to make sure selection remains stable after an action
		    	var selection = window.getSelection();
		    	var hasValidSelection = false;
		    	if (selection.rangeCount) {
		    		var $anchor = $(selection.anchorNode);
		    		var $focus = $(selection.focusNode);
		    		if ($anchor[0] != $focus[0] || selection.anchorOffset != selection.focusOffset) { // something must be selected
		    			var $anchorDiff = $anchor.closest(".text-diff");
		    			var $focusDiff = $focus.closest(".text-diff");
		    			if ($anchorDiff.length != 0 && $focusDiff.length != 0 && $anchorDiff[0] == $focusDiff[0]) { // selection must be within same file
		    				var $anchorTd = $anchor;
		    				if (!$anchorTd.is("td.content"))
		    					$anchorTd = $anchor.closest(".text-diff td.content");
		    				var $focusTd = $focus;
		    				if (!$focusTd.is("td.content"))
		    					$focusTd = $focus.closest(".text-diff td.content");
		    					
		    				if ($anchorTd.length != 0 && $focusTd.length != 0) { // selection must starts with diff content and ends with diff content 
		    					if ($anchorTd.hasClass("left") && $focusTd.hasClass("left") 
		    							|| $anchorTd.hasClass("right") && $focusTd.hasClass("right") 
		    							|| !$anchorTd.hasClass("left") && !$anchorTd.hasClass("right")) { // selection must not span the split view
			    					var $anchorTr = $anchorTd.closest("tr");
			    					var $focusTr = $focusTd.closest("tr");
			    					if ($anchorTr.nextAll("tr.expander").filter($focusTr.prevAll("tr.expander")).length == 0
			    							&& $anchorTr.prevAll("tr.expander").filter($focusTr.nextAll("tr.expander")).length == 0) { // all lines between selection has been expanded 
			    						var range = selection.getRangeAt(0).cloneRange();
			    						range.collapse(true);
			    						var startRect = range.getClientRects()[0];
			    						range = selection.getRangeAt(0).cloneRange();
			    						range.collapse(false);
			    						var endRect = range.getClientRects()[0];
			    						if (startRect && endRect) {
			    							hasValidSelection = true;
			    			    			var position = {
			    			    				left: $(window).scrollLeft() + (startRect.left + endRect.left)/2,
			    			    				top: $(window).scrollTop() + startRect.top
			    			    			}
			    				    		$("#selection-popup").data("show")(position, "www.pmease.com", function(){}, $container[0]);
			    						}
			    					}
		    					}
		    				}
		    			}
		    		}
		    	}
		    	if (!hasValidSelection) {
		    		$("#selection-popup").hide();	    		
		    	}	    		
	    	}, 100);
	    });
	}
}

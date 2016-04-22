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
		    		var firstRange = selection.getRangeAt(0).cloneRange();
		    		var lastRange = selection.getRangeAt(selection.rangeCount-1).cloneRange();
		    		var $anchor = $(firstRange.startContainer);
		    		var $focus = $(lastRange.endContainer);
		    		var anchorOffset = firstRange.startOffset;
		    		var focusOffset = lastRange.endOffset;
		    		if ($anchor[0] != $focus[0] || selection.anchorOffset != selection.focusOffset) { // something must be selected
		    			var $anchorDiff = $anchor;
		    			if (!$anchorDiff.hasClass("text-diff"))
		    				$anchorDiff = $anchor.closest(".text-diff");
		    			var $focusDiff = $focus;
		    			if (!$focusDiff.hasClass("text-diff"))
		    				$focusDiff = $focus.closest(".text-diff");
		    			
		    			if ($anchorDiff.length != 0 && $focusDiff.length != 0 && $anchorDiff[0] == $focusDiff[0]) { // selection must be within same file
		    				var $anchorTd = $anchor;
		    				if (!$anchorTd.is("td.content"))
		    					$anchorTd = $anchor.closest(".text-diff td.content");
		    				var $focusTd = $focus;
		    				if (!$focusTd.is("td.content"))
		    					$focusTd = $focus.closest(".text-diff td.content");

		    				if ($anchorTd.length != 0 || $focusTd.length != 0) {
		    					if ($anchorTd.length == 0) {
	    							var $tr;
		    						if ($anchor.is("tr.code")) {
		    							$tr = $anchor;
		    						} else {
		    							$tr = $anchorDiff.find("tr.code").first();
		    						}
	    							if ($focusTd.hasClass("left"))
	    								$anchorTd = $tr.find("td.content.left");
	    							else if ($focusTd.hasClass("right"))
	    								$anchorTd = $tr.find("td.content.right");
	    							else
	    								$anchorTd = $tr.find("td.content");
	    							$anchor = $anchorTd;
	    							anchorOffset = -1;
		    					}
		    					if ($focusTd.length == 0) {
	    							var $tr;
	    							if ($focus.is("tr.code")) {
	    								$tr = $focus;
	    							} else {
		    							$tr = $focusDiff.find("tr.code").last();
		    						} 
	    							if ($anchorTd.hasClass("left"))
	    								$focusTd = $tr.find("td.content.left");
	    							else if ($anchorTd.hasClass("right"))
	    								$focusTd = $tr.find("td.content.right");
	    							else
	    								$focusTd = $tr.find("td.content");
	    							$focus = $focusTd;
	    							focusOffset = -1;
		    					}
		    				}
		    				
		    				if ($anchorTd.length != 0 && $focusTd.length != 0) { // selection must starts with diff content and ends with diff content 
		    					if ($anchorTd.hasClass("left") && $focusTd.hasClass("left") 
		    							|| $anchorTd.hasClass("right") && $focusTd.hasClass("right") 
		    							|| !$anchorTd.hasClass("left") && !$anchorTd.hasClass("right")) { // selection must not span the split view
			    					var $anchorTr = $anchorTd.closest("tr");
			    					var $focusTr = $focusTd.closest("tr");
			    					if ($anchorTr.nextAll("tr.expander").filter($focusTr.prevAll("tr.expander")).length == 0
			    							&& $anchorTr.prevAll("tr.expander").filter($focusTr.nextAll("tr.expander")).length == 0) { // all lines between selection has been expanded 
		    							hasValidSelection = true;
		    							firstRange.collapse(true);
		    							var startRect = firstRange.getClientRects()[0];
		    							lastRange.collapse(false);
		    							var endRect = lastRange.getClientRects()[0];
		    							if (!startRect || !endRect) {
				    						startRect = selection.getRangeAt(0).getClientRects()[0];
				    						endRect = startRect;
		    							}
		    							var position = {
		    								left: (startRect.left + endRect.right)/2,
		    								top: startRect.top
		    							};
		    			    			position.left += $(window).scrollLeft();
		    			    			position.top += $(window).scrollTop();
		    				    		$("#selection-popup").data("show")(position, "www.pmease.com", function(){}, $container[0]);
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

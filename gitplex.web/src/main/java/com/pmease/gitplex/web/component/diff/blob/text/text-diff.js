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
		    			    			
		    				    		function getCursor($td, $node, nodeOffset) {
		    				    			var oldLine, newLine;
		    				    			var oldCh, newCh;
	    				    				if (!$td.hasClass("old") && !$td.hasClass("new") 
	    				    						|| $td.hasClass("old") && $td.hasClass("new")) {
	    				    					oldLine = $td.data("old") + 1;
	    				    					newLine = $td.data("new") + 1;
	    				    					oldCh = newCh = $td.text().length;
	    				    				} else if ($td.hasClass("old")) {
	    				    					oldLine = $td.data("old") + 1;
	    				    					oldCh = $td.text().length;
	    				    				} else {
	    				    					newLine = $td.data("new") + 1;
	    				    					newCh = $td.text().length;
	    				    				}
		    				    			if ($node[0] != $td[0]) {
	    										var oldOffset = 0, newOffset = 0;
			    				    			var $children = $td.contents();
			    				    			if ($node.parent().is("span"))
			    				    				$node = $node.parent();
			    				    			for (var i=0; i<$children.length; i++) {
			    				    				var $child = $($children[i]);
			    				    				if ($child[0] == $node[0]) {
			    				    					if ($child.hasClass("delete")) { 
			    				    						oldCh = oldOffset + nodeOffset;
			    				    						newCh = undefined;
			    				    						newLine = undefined;
			    				    					} else if ($child.hasClass("insert")) {
			    				    						oldCh = undefined;
			    				    						oldLine = undefined;
			    				    						newCh = newOffset + nodeOffset;
			    				    					} else {
			    				    						oldCh = oldOffset + nodeOffset;
			    				    						newCh = newOffset + nodeOffset;
			    				    					}
			    				    					break;
			    				    				} else {
			    				    					var len = $child.text().length;
			    				    					if ($child.hasClass("delete")) {
			    				    						oldOffset += len;
			    				    					} else if ($child.hasClass("insert")) {
			    				    						newOffset += len;
			    				    					} else {
				    				    					oldOffset += len;
				    				    					newOffset += len;
			    				    					}
			    				    				}
			    				    			}
		    				    			}
	    									return {
	    										oldLine: oldLine,
	    										oldCh: oldCh,
	    										newLine: newLine,
	    										newCh: newCh
	    									};
		    				    		}

		    				    		var anchorCursor = getCursor($anchorTd, $anchor, firstRange.startOffset);
		    				    		var focusCursor = getCursor($focusTd, $focus, lastRange.endOffset);

		    				    		var mark;
		    				    		var commentCallback = function() {};
		    				    		var unableToCommentUrl = "http://wiki.pmease.com/display/gp/Add+Diff+Comment";
		    				    		if (anchorCursor.newLine) {
		    				    			if (focusCursor.newLine) {
		    				    				mark = "new." + anchorCursor.newLine+"."+anchorCursor.newCh 
		    				    						+ "-new." + focusCursor.newLine + "." + focusCursor.newCh;		    				    			
		    				    			} else {
		    				    				mark = "new." + anchorCursor.newLine+"."+anchorCursor.newCh 
		    				    						+ "-old." + focusCursor.oldLine + "." + focusCursor.oldCh;		    				    			
		    				    				commentCallback = unableToCommentUrl;
		    				    			}
		    				    		} else {
		    				    			if (focusCursor.oldLine) {
		    				    				mark = "old." + anchorCursor.oldLine+"."+anchorCursor.oldCh 
		    				    						+ "-old." + focusCursor.oldLine + "." + focusCursor.oldCh;		    				    			
		    				    			} else {
		    				    				mark = "old." + anchorCursor.oldLine+"."+anchorCursor.oldCh 
		    				    						+ "-new." + focusCursor.newLine + "." + focusCursor.newCh;		    				    			
		    				    				commentCallback = unableToCommentUrl;
		    				    			}
		    				    		}
		    			    			var uri = URI(window.location.href); 
		    			    			uri.removeSearch("path").addSearch("path", $container.data("path"));
		    			    			uri.removeSearch("mark").addSearch("mark", mark);
		    				    		$("#selection-popup").data("show")(position, uri.toString(), 
		    				    				commentCallback, $container[0]);
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

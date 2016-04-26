gitplex.textdiff = {
	symbolClasses: ".cm-property, .cm-variable, .cm-variable-2, .cm-variable-3, .cm-def, .cm-meta",
	init: function(containerId, symbolTooltipId, oldRev, newRev) {
		var $container = $("#" + containerId);
		$container.data("symbolHover", function() {
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
		var $symbols = $container.find(gitplex.textdiff.symbolClasses); 
		$symbols.mouseover($container.data("symbolHover"));
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
		    	var displaySelectionPopup = false;
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
		    			
		    			if ($anchorDiff.length != 0 && $focusDiff.length != 0 && $anchorDiff.is($focusDiff)) { // selection must be within same file
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
		    							anchorOffset = 0;
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
		    							focusOffset = -1;
		    						} 
	    							if ($anchorTd.hasClass("left"))
	    								$focusTd = $tr.find("td.content.left");
	    							else if ($anchorTd.hasClass("right"))
	    								$focusTd = $tr.find("td.content.right");
	    							else
	    								$focusTd = $tr.find("td.content");
	    							$focus = $focusTd;
	    							if (focusOffset == -1)
	    								focusOffset = $focusTd.text().length;
		    					}
		    				}
		    				
		    				if ($anchorTd.length != 0 && $focusTd.length != 0) { // selection must starts with diff content and ends with diff content 
		    					if ($anchorTd.hasClass("left") && $focusTd.hasClass("left") 
		    							|| $anchorTd.hasClass("right") && $focusTd.hasClass("right") 
		    							|| !$anchorTd.hasClass("left") && !$anchorTd.hasClass("right")) { // selection must not span the split view
		    						displaySelectionPopup = true;
	    							
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

	    			    			var markPos;
			    					var $anchorTr = $anchorTd.closest("tr");
			    					var $focusTr = $focusTd.closest("tr");
			    					
			    					if ($anchor.is("td.content") && anchorOffset == 0 && $anchorTr.next().is("tr.expander")) {
			    						if ($anchor.hasClass("left")) {
			    							$anchor = $anchorTr.next().next().find("td.content.left");
			    							$anchorTd = $anchor;
			    							anchorOffset = 0;
			    						} else if ($anchor.hasClass("right")) {
			    							$anchor = $anchorTr.next().next().find("td.content.right");
			    							$anchorTd = $anchor;
			    							anchorOffset = 0;
			    						} else {
			    							$anchor = $anchorTr.next().next().find("td.content");
			    							$anchorTd = $anchor;
			    							anchorOffset = 0;
			    						}
			    						$anchorTr = $anchorTd.parent();
			    					}
			    					if ($focus.is("td.content") && focusOffset == 0 && $focusTr.prev().is("tr.expander")) {
			    						if ($focus.hasClass("left")) {
			    							$focus = $focusTr.prev().prev().find("td.content.left");
			    							$focusTd = $focus;
			    							focusOffset = $focus.text().length;
			    						} else if ($focus.hasClass("right")) {
			    							$focus = $focusTr.prev().prev().find("td.content.right");
			    							$focusTd = $focus;
			    							focusOffset = $focus.text().length;
			    						} else {
			    							$focus = $focusTr.prev().prev().find("td.content");
			    							$focusTd = $focus;
			    							focusOffset = $focus.text().length;
			    						}
			    						$focusTr = $focusTd.parent();
			    					}
			    					
			    					if ($anchorTr.nextAll("tr.expander").filter($focusTr.prevAll("tr.expander")).length == 0
			    							&& $anchorTr.prevAll("tr.expander").filter($focusTr.nextAll("tr.expander")).length == 0) { // all lines between selection has been expanded 
			    						
		    				    		function getCursor($td, $node, nodeOffset) {
		    				    			var oldLine, newLine;
		    				    			var oldCh, newCh;
	    				    				if (!$td.hasClass("old") && !$td.hasClass("new") 
	    				    						|| $td.hasClass("old") && $td.hasClass("new")) {
	    				    					oldLine = $td.data("old") + 1;
	    				    					newLine = $td.data("new") + 1;
	    				    					oldCh = newCh = nodeOffset;
	    				    				} else if ($td.hasClass("old")) {
	    				    					oldLine = $td.data("old") + 1;
	    				    					oldCh = nodeOffset;
	    				    				} else {
	    				    					newLine = $td.data("new") + 1;
	    				    					newCh = nodeOffset;
	    				    				}
		    				    			if (!$node.is($td)) {
	    										var oldOffset = 0, newOffset = 0;
			    				    			var $children = $td.contents();
			    				    			if ($node.parent().is("span"))
			    				    				$node = $node.parent();
			    				    			for (var i=0; i<$children.length; i++) {
			    				    				var $child = $($children[i]);
			    				    				if ($child.is($node)) {
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
		    				    			if ($td.hasClass("left")) {
		    				    				newLine = newCh = undefined;
		    				    			} else if ($td.hasClass("right")) {
		    				    				oldLine = oldCh = undefined;
		    				    			}
	    									return {
	    										oldLine: oldLine,
	    										oldCh: oldCh,
	    										newLine: newLine,
	    										newCh: newCh
	    									};
		    				    		}
	
		    				    		var anchorCursor = getCursor($anchorTd, $anchor, anchorOffset);
		    				    		var focusCursor = getCursor($focusTd, $focus, focusOffset);
	
		    				    		if (anchorCursor.newLine && focusCursor.newLine) {
		    				    			if (anchorCursor.newLine == focusCursor.newLine && anchorCursor.newCh < focusCursor.newCh
		    				    					|| anchorCursor.newLine < focusCursor.newLine) {
		    				    				markPos = "new-" + anchorCursor.newLine + "." + anchorCursor.newCh 
		    				    						+ "-" + focusCursor.newLine + "." + focusCursor.newCh;		    				    			
		    				    			} else {
		    				    				displaySelectionPopup = false;
		    				    			}
		    				    		} else if (anchorCursor.oldLine && focusCursor.oldLine) {
		    				    			if (anchorCursor.oldLine == focusCursor.oldLine && anchorCursor.oldCh < focusCursor.oldCh
		    				    					|| anchorCursor.oldLine < focusCursor.oldLine) {
		    				    				markPos = "old-" + anchorCursor.oldLine + "." + anchorCursor.oldCh 
		    				    						+ "-" + focusCursor.oldLine + "." + focusCursor.oldCh;		    				    			
		    				    			} else {
		    				    				displaySelectionPopup = false;
		    				    			}
		    				    		}
			    					}
	    				    		var invalidSelectionUrl = "http://wiki.pmease.com/display/gp/Diff+Selection";
	    			    			var permanentCallback = function($permanentLink) {
	    			    				$permanentLink.off("click");
	    				    			if (markPos) {
			    			    			var uri = URI(window.location.href); 
			    			    			var markFile = $container.data("markfile");
			    			    			uri.fragment({
			    			    				markfile: markFile,
			    			    				markpos: markPos
			    			    			});
		    			    				$permanentLink.attr("href", uri.toString());
		    			    				$permanentLink.html("<i class='fa fa-link'></i> Permanent link of this selection");
		    			    				$permanentLink.click(function() {
		    				    				window.getSelection().removeAllRanges();
		    				    				// continue to operate DOM in a timer to give browser a chance to 
		    				    				// clear selections
		    				    				setTimeout(function() {
			    				    				gitplex.textdiff.clearMarks();
			    				    				$("#selection-popup").hide();
			    			    					history.pushState(undefined, '', uri.toString());
			    			    					gitplex.textdiff.mark(markFile, markPos);
		    				    				}, 100);
		    			    					return false;
		    			    				});
	    				    			} else {
	    				    				$permanentLink.attr("href", invalidSelectionUrl);
	    				    				$permanentLink.html("<i class='fa fa-link'></i> No permanent link to this selection, see why");
	    				    			}
	    			    			}
	    				    		var commentCallback = function($commentLink) {
	    				    			if (markPos) {
	    				    				$commentLink.removeAttr("href");
	    				    				$commentLink.html("<i class='fa fa-comment'></i> Add comment for this selection");
	    				    			} else {
	    				    				$commentLink.attr("href", invalidSelectionUrl);
	    				    				$commentLink.html("<i class='fa fa-comment'></i> Unable to comment this selection, see why");
	    				    			}
	    				    		};
	    				    		$("#selection-popup").data("show")(position, permanentCallback, 
	    				    				commentCallback, $container[0]);
		    					}
		    				}
		    			}
		    		}
		    	}
		    	if (!displaySelectionPopup) {
		    		$("#selection-popup").hide();	    		
		    	}	    		
	    	}, 100);
	    });
		var uri = URI(window.location.href); 
		var fragment = uri.fragment(true);
		if ($container.data("markfile") == fragment.markfile && fragment.markpos) {
			gitplex.textdiff.mark(fragment.markfile, fragment.markpos);	
		}
	},
	expand: function(containerId, blockIndex, expandedHtml) {
		var $container = $("#" + containerId);
		var $expanderTr = $container.find(".expander" + blockIndex);
		var $prevTr = $expanderTr.prev();
		var $nextTr = $expanderTr.next();
		$expanderTr.replaceWith(expandedHtml); 
		var $expandedTrs;
		if ($prevTr.length != 0 && $nextTr.length != 0) {
			$expandedTrs = $prevTr.nextAll().filter($nextTr.prevAll());
		} else if ($prevTr.length != 0) {
			$expandedTrs = $prevTr.nextAll();
		} else {
			$expandedTrs = $nextTr.prevAll();
		}
		var $symbols = $expandedTrs.find(gitplex.textdiff.symbolClasses); 
		$symbols.mouseover($container.data("symbolHover"));
	},
	getMarkInfo: function(markFile, markPos) {
		var $container = $('*[data-markfile="' + markFile.escape() + '"]');
		var splitted = markPos.split("-");
		var oldOrNew = splitted[0];
		var anchorCursor = splitted[1].split(".");
		var focusCursor = splitted[2].split(".");
		var $anchorTd = $container.find("td.content[data-" + oldOrNew + "='" + (anchorCursor[0]-1) + "']");
		var $focusTd = $container.find("td.content[data-" + oldOrNew + "='" + (focusCursor[0]-1) + "']");
		if ($anchorTd.length == 0) { 
			console.error("Unable to find anchor td!");
			$anchorTd = undefined;
		} else if ($anchorTd.length == 2) {
			if (oldOrNew == "old") {
				$anchorTd = $anchorTd.first();
			} else {
				$anchorTd = $anchorTd.last();
			}
		}
		if ($focusTd.length == 0) {
			console.error("Unable to find focus td!");
			$focusTd = undefined;
		} else if ($focusTd.length == 2) {
			if (oldOrNew == "old") {
				$focusTd = $focusTd.first();
			} else {
				$focusTd = $focusTd.last();
			}
		}
		return {
			anchorTd: $anchorTd,
			anchorCursor: anchorCursor,
			focusTd: $focusTd,
			focusCursor: focusCursor,
			oldOrNew: oldOrNew
		};
	},
	scroll: function(markFile, markPos) {
		var markInfo = gitplex.textdiff.getMarkInfo(markFile, markPos);
		var $anchorTd = markInfo.anchorTd;
		var $focusTd = markInfo.focusTd;
		if ($anchorTd && $focusTd) {
			var anchorTop = $anchorTd.offset().top;
			var focusBottom = $focusTd.offset().top + $focusTd.outerHeight();
			var markHeight = focusBottom - anchorTop;
			var stickyHeight = $(".sticky").outerHeight();
			var screenHeight = $(window).height() - stickyHeight;
			var availableHeight = screenHeight - markHeight;
			var scrollTop = $anchorTd.offset().top - stickyHeight;
			if (availableHeight > 0) {
				// we have enough screen space, so do not put on top of screen for easier reading
				scrollTop -= availableHeight/4;
			} 
			$(window).scrollTop(scrollTop);
		}
	},
	mark: function(markFile, markPos) {
		var markInfo = gitplex.textdiff.getMarkInfo(markFile, markPos);
		var $anchorTd = markInfo.anchorTd;
		var $focusTd = markInfo.focusTd;
		if ($anchorTd && $focusTd) {
			var anchorCursor = markInfo.anchorCursor;
			var focusCursor = markInfo.focusCursor;
			var oldOrNew = markInfo.oldOrNew;
			var $td = $anchorTd;
			while (true) {
				var ch = 0;
				$td.addClass("content-mark");
				$td.data("beforemark", $td.html());
				$td.contents().each(function() {
					var $this = $(this);
					var text = $this.text();
					function markText(from, to) {
						var text = $this.text();
						var left = text.substring(0, from);
						var middle = text.substring(from, to);
						var right = text.substring(to);
						
						if (left.length == 0 && right.length == 0 && $this.is("span")) {
							$this.addClass("content-mark");
						} else {
							var classes;
							if ($this.is("span"))
								classes = $this.attr("classes");
							else
								classes = "";
							
							var $current = $this;
							if (left.length != 0) {
								$current.after("<span></span>");
								$current = $current.next();
								$current.attr("class", classes).text(left);
							}
							
							$current.after("<span></span>");
							$current = $current.next();
							$current.attr("class", classes + " content-mark").text(middle);
							
							if (right.length != 0) {
								$current.after("<span></span>");
								$current = $current.next();
								$current.attr("class", classes).text(right);
							}
							$this.remove();
						}
					}
					if ($this.hasClass("insert") && oldOrNew == "old" 
							|| $this.hasClass("delete") && oldOrNew == "new") {
						if (!$td.is($anchorTd) && !$td.is($focusTd)) {
							markText(0, text.length);
						} else if ($td.is($anchorTd) && $td.is($focusTd)) {
							if (focusCursor[1]>ch && anchorCursor[1]<ch) {
								markText(0, text.length);
							}
						} else if ($td.is($anchorTd)) {
							if (anchorCursor[1]<ch) {
								markText(0, text.length);
							}
						} else {
							if (focusCursor[1]>ch) {
								markText(0, text.length);
							}
						}
					} else {
						var nextCh = ch + text.length;
						if (!$td.is($anchorTd) && !$td.is($focusTd)) {
							markText(0, text.length);
						} else if ($td.is($anchorTd) && $td.is($focusTd)) {
							if (focusCursor[1]>ch && anchorCursor[1]<nextCh) {
								if (ch>=anchorCursor[1] && nextCh<=focusCursor[1]) {
									markText(0, text.length);
								} else if (ch<anchorCursor[1] && nextCh>focusCursor[1]) {
									markText(anchorCursor[1]-ch, focusCursor[1]-ch);
								} else if (ch<anchorCursor[1]) {
									markText(anchorCursor[1]-ch, text.length);
								} else {
									markText(0, focusCursor[1]-ch);
								}
							}
						} else if ($td.is($anchorTd)) {
							if (ch>=anchorCursor[1]) {
								markText(0, text.length);
							} else if (nextCh>anchorCursor[1]) {
								markText(anchorCursor[1]-ch, text.length);
							} 
						} else {
							if (nextCh<=focusCursor[1]) {
								markText(0, text.length);
							} else if (ch<focusCursor[1]) {
								markText(0, focusCursor[1]-ch);
							} 
						}
						ch = nextCh;
					}
				});
				if ($td.is($focusTd) || $td.length == 0) {
					break;
				} else {
					if ($anchorTd.hasClass("left")) {
						$td = $td.parent().next().children("td.left");
					} else if ($anchorTd.hasClass("right")) {
						$td = $td.parent().next().children("td.right");
					} else {
						$td = $td.parent().next().children("td.content");
					}
				}
			}			
		}
	}, 
	clearMarks: function() {
		$("td.content.content-mark").each(function() {
			var $this = $(this);
			$this.html($this.data("beforemark"));
			$this.removeClass("content-mark");
			$this.removeData("beforemark");
			$container = $this.closest(".text-diff").parent();
			$this.find(gitplex.textdiff.symbolClasses).mouseover($container.data("symbolHover"));
		});
	}
}

$(function() {
	var uri = URI(window.location.href); 
	var fragment = uri.fragment(true);
	if (fragment.markfile && fragment.markpos) {
		gitplex.textdiff.scroll(fragment.markfile, fragment.markpos);	
	}
	$(document).on("popstate", function(e) {
		e.stopPropagation();
		gitplex.textdiff.clearMarks();
		var uri = URI(window.location.href); 
		var fragment = uri.fragment(true);
		if (fragment.markfile && fragment.markpos) {
			gitplex.textdiff.mark(fragment.markfile, fragment.markpos);	
		}
	});
});

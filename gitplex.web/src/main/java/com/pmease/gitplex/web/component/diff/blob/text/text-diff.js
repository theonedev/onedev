gitplex.textdiff = {
	symbolClasses: ".cm-property, .cm-variable, .cm-variable-2, .cm-variable-3, .cm-def, .cm-meta",
	init: function(containerId, symbolTooltipId, oldRev, newRev, scroll, callback) {
		var $container = $("#" + containerId);
		$container.data("callback", callback);
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
		$container.data("onMouseOverContent", function() {
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
		var $symbols = $container.find(gitplex.textdiff.symbolClasses); 
		$symbols.mouseover($container.data("symbolHover"));
		$container.find("td.content").mouseover($container.data("onMouseOverContent"));
		$container.on("mouseup keyup", function() {
	    	setTimeout(function() { // use a timeout to make sure selection remains stable after an action
		    	var selection = window.getSelection();
		    	var displaySelectionPopup = false;
		    	if (selection.rangeCount) { 
		    		var firstRange = selection.getRangeAt(0).cloneRange();
		    		var lastRange = selection.getRangeAt(selection.rangeCount-1).cloneRange();
		    		var $start = $(firstRange.startContainer);
		    		var $end = $(lastRange.endContainer);
		    		var startOffset = firstRange.startOffset;
		    		var endOffset = lastRange.endOffset;
		    		if (!$start.is($end) || startOffset != endOffset) { // something must be selected
		    			var $startDiff;
		    			if (!$start.hasClass("text-diff"))
		    				$startDiff = $start.closest(".text-diff");
		    			else
		    				$startDiff = $start;
		    			
		    			var $endDiff;
		    			if (!$end.hasClass("text-diff"))
		    				$endDiff = $end.closest(".text-diff");
		    			else
		    				$endDiff = $end;
		    			
		    			if ($startDiff.length != 0 && $endDiff.length != 0 && $startDiff.is($endDiff)) { // selection must be within same file
			    			function getStartTextNodeAndOffset($td, index) {
			    				var $children = $td.contents();
				    			for (var i=0; i<$children.length; i++) {
				    				if (i == index) {
				    					return {
				    						node: $($children[i]),
				    						offset: 0
				    					};
				    				}
				    			}		    		
				    			return {node: $td, offset: 0};
			    			}
			    			function getEndTextNodeAndOffset($td, index) {
			    				var $children = $td.contents();
				    			for (var i=0; i<$children.length; i++) {
				    				var $child = $($children[i]);
				    				if (i == index-1) {
				    					return {
				    						node: $child,
				    						offset: $child.text().length
				    					};
				    				}
				    			}		    		
				    			return {node: $td, offset: $children.length};
			    			}
		    				
		    				/*
		    				 * in case start or end node is table cell (for instance when triple click 
		    				 * a line in firefox), the offset is index of child node inside the 
		    				 * table cell, so we convert them to child node and corresponding index
		    				 */
		    				var $startTd = $start.is("td.content")? $start: $start.closest(".text-diff td.content");
		    				var $endTd = $end.is("td.content")? $end: $end.closest(".text-diff td.content");

		    				if ($startTd.length != 0 || $endTd.length != 0) { // at least one side of selection must be within a table cell
		    					var $referenceTd = $startTd.length != 0? $startTd: $endTd;
		    					
		    					function getSameSideTd($tr, $referenceTd) {
		    						if ($referenceTd.hasClass("left"))
		    							return $tr.find("td.content.left");
		    						else if ($referenceTd.hasClass("right"))
		    							return $tr.find("td.content.right");
		    						else
		    							return $tr.find("td.content");
		    					}

		    					if (!$start.parent().is("td.content")) {
		    						var $td;
				    				if ($start.is("td.content")) { 
				    					$td = $start;
				    				} else if ($start.is("tr.code")) {
				    					$td = getSameSideTd($start, $referenceTd);
				    					startOffset = 0;
				    				} else if ($start.is("tr.expander")) {
				    					$td = getSameSideTd($start.next(), $referenceTd);
				    					startOffset = 0;
				    				} else {
		    							$tr = $startDiff.find("tr.code").first();
		    							startOffset = 0;
				    				}
			    					var nodeAndOffset = getStartTextNodeAndOffset($td, startOffset);
			    					$start = nodeAndOffset.node;
			    					startOffset = nodeAndOffset.offset;
		    					}
			    				
		    					if (!$end.parent().is("td.content")) {
		    						var $td;
				    				if ($end.is("td.content")) { 
				    					$td = $end;
				    				} else if ($end.is("tr.code")) {
				    					$td = getSameSideTd($end, $referenceTd);
				    					endOffset = $td.contents().length;
				    				} else if ($end.is("tr.expander")) {
				    					$td = getSameSideTd($end.prev(), $referenceTd);
				    					endOffset = $td.contents().length;
				    				} else {
		    							$tr = $endDiff.find("tr.code").last();
		    							endOffset = $td.contents().length;
				    				}
			    					var nodeAndOffset = getEndTextNodeAndOffset($td, endOffset);
			    					$end = nodeAndOffset.node;
			    					endOffset = nodeAndOffset.offset;
		    					}
		    				}
		    				
		    				if ($startTd.length != 0 && $endTd.length != 0) { // selection must starts with diff content and ends with diff content 
		    					if ($startTd.hasClass("left") && $endTd.hasClass("left") 
		    							|| $startTd.hasClass("right") && $endTd.hasClass("right") 
		    							|| !$startTd.hasClass("left") && !$startTd.hasClass("right")) { // selection must not span the split view
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
			    					var $startTr = $startTd.closest("tr");
			    					var $endTr = $endTd.closest("tr");

			    					// drag mouse over a line above expander, chrome may set $end as the line 
			    					// below expander, below code handles this case
			    					if ($end.is("td.content") && endOffset == 0 && $endTr.prev().is("tr.expander")) {
			    						if ($end.hasClass("left")) {
			    							$end = $endTr.prev().prev().find("td.content.left");
			    							$endTd = $end;
			    							endOffset = $end.text().length;
			    						} else if ($end.hasClass("right")) {
			    							$end = $endTr.prev().prev().find("td.content.right");
			    							$endTd = $end;
			    							endOffset = $end.text().length;
			    						} else {
			    							$end = $endTr.prev().prev().find("td.content");
			    							$endTd = $end;
			    							endOffset = $end.text().length;
			    						}
			    						$endTr = $endTd.parent();
			    					}
			    					
			    					if ($startTr.nextAll("tr.expander").filter($endTr.prevAll("tr.expander")).length == 0
			    							&& $startTr.prevAll("tr.expander").filter($endTr.nextAll("tr.expander")).length == 0) { // all lines between selection has been expanded 
			    						
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
	
		    				    		var startCursor = getCursor($startTd, $start, startOffset);
		    				    		var endCursor = getCursor($endTd, $end, endOffset);
	
		    				    		if (startCursor.newLine && endCursor.newLine) {
		    				    			if (startCursor.newLine == endCursor.newLine && startCursor.newCh < endCursor.newCh
		    				    					|| startCursor.newLine < endCursor.newLine) {
		    				    				markPos = "new-" + startCursor.newLine + "." + startCursor.newCh 
		    				    						+ "-" + endCursor.newLine + "." + endCursor.newCh;		    				    			
		    				    			} else {
		    				    				displaySelectionPopup = false;
		    				    			}
		    				    		} else if (startCursor.oldLine && endCursor.oldLine) {
		    				    			if (startCursor.oldLine == endCursor.oldLine && startCursor.oldCh < endCursor.oldCh
		    				    					|| startCursor.oldLine < endCursor.oldLine) {
		    				    				markPos = "old-" + startCursor.oldLine + "." + startCursor.oldCh 
		    				    						+ "-" + endCursor.oldLine + "." + endCursor.oldCh;		    				    			
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
			    			    			uri.removeSearch("mark-file").addSearch("mark-file", markFile);
			    			    			uri.removeSearch("mark-pos").addSearch("mark-pos", markPos);
		    			    				$permanentLink.attr("href", uri.toString());
		    			    				$permanentLink.html("<i class='fa fa-link'></i> Permanent link of this selection");
		    			    				$permanentLink.click(function(e) {
		    			    					e.preventDefault();
		    				    				window.getSelection().removeAllRanges();
	    				    					$container.data("callback")("storeUrl", uri.toString());
		    				    				// continue to operate DOM in a timer to give browser a chance to 
		    				    				// clear selections
		    				    				setTimeout(function() {
			    				    				gitplex.textdiff.clearMarks();
			    				    				$("#selection-popup").hide();
			    			    					pmease.commons.history.pushState(uri.toString());
			    			    					gitplex.textdiff.mark(markFile, markPos);
		    				    				}, 100);
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
		var search = uri.search(true);
		if ($container.data("markfile") == search["mark-file"] && search["mark-pos"]) {
			gitplex.textdiff.mark(search["mark-file"], search["mark-pos"]);	
			if (scroll) 
				gitplex.textdiff.scroll(search["mark-file"], search["mark-pos"]);
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
		$expandedTrs.find("td.content").mouseover($container.data("onMouseOverContent"));
	},
	getMarkInfo: function(markFile, markPos) {
		var $container = $('*[data-markfile="' + markFile.escape() + '"]');
		var splitted = markPos.split("-");
		var oldOrNew = splitted[0];
		var startCursor = splitted[1].split(".");
		var endCursor = splitted[2].split(".");
		var $startTd = $container.find("td.content[data-" + oldOrNew + "='" + (startCursor[0]-1) + "']");
		var $endTd = $container.find("td.content[data-" + oldOrNew + "='" + (endCursor[0]-1) + "']");
		if ($startTd.length == 0) { 
			console.error("Unable to find start td!");
			$startTd = undefined;
		} else if ($startTd.length == 2) {
			if (oldOrNew == "old") {
				$startTd = $startTd.first();
			} else {
				$startTd = $startTd.last();
			}
		}
		if ($endTd.length == 0) {
			console.error("Unable to find end td!");
			$endTd = undefined;
		} else if ($endTd.length == 2) {
			if (oldOrNew == "old") {
				$endTd = $endTd.first();
			} else {
				$endTd = $endTd.last();
			}
		}
		return {
			startTd: $startTd,
			startCursor: startCursor,
			endTd: $endTd,
			endCursor: endCursor,
			oldOrNew: oldOrNew
		};
	},
	scroll: function(markFile, markPos) {
		var markInfo = gitplex.textdiff.getMarkInfo(markFile, markPos);
		var $startTd = markInfo.startTd;
		var $endTd = markInfo.endTd;
		if ($startTd && $endTd) {
			var startTop = $startTd.offset().top;
			var endBottom = $endTd.offset().top + $endTd.outerHeight();
			var markHeight = endBottom - startTop;
			var stickyHeight = $(".sticky").outerHeight();
			var screenHeight = $(window).height() - stickyHeight;
			var availableHeight = screenHeight - markHeight;
			var scrollTop = $startTd.offset().top - stickyHeight;
			if (availableHeight > 0) {
				// we have enough screen space, so do not put on top of screen for easier reading
				scrollTop -= availableHeight/4;
			} 
			$(window).scrollTop(scrollTop);
		}
	},
	mark: function(markFile, markPos) {
		var markInfo = gitplex.textdiff.getMarkInfo(markFile, markPos);
		var $startTd = markInfo.startTd;
		var $endTd = markInfo.endTd;
		if ($startTd && $endTd) {
			var startCursor = markInfo.startCursor;
			var endCursor = markInfo.endCursor;
			var oldOrNew = markInfo.oldOrNew;
			var $td = $startTd;
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
								classes = $this.attr("class");
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
						if (!$td.is($startTd) && !$td.is($endTd)) {
							markText(0, text.length);
						} else if ($td.is($startTd) && $td.is($endTd)) {
							if (endCursor[1]>ch && startCursor[1]<ch) {
								markText(0, text.length);
							}
						} else if ($td.is($startTd)) {
							if (startCursor[1]<ch) {
								markText(0, text.length);
							}
						} else {
							if (endCursor[1]>ch) {
								markText(0, text.length);
							}
						}
					} else {
						var nextCh = ch + text.length;
						if (!$td.is($startTd) && !$td.is($endTd)) {
							markText(0, text.length);
						} else if ($td.is($startTd) && $td.is($endTd)) {
							if (endCursor[1]>ch && startCursor[1]<nextCh) {
								if (ch>=startCursor[1] && nextCh<=endCursor[1]) {
									markText(0, text.length);
								} else if (ch<startCursor[1] && nextCh>endCursor[1]) {
									markText(startCursor[1]-ch, endCursor[1]-ch);
								} else if (ch<startCursor[1]) {
									markText(startCursor[1]-ch, text.length);
								} else {
									markText(0, endCursor[1]-ch);
								}
							}
						} else if ($td.is($startTd)) {
							if (ch>=startCursor[1]) {
								markText(0, text.length);
							} else if (nextCh>startCursor[1]) {
								markText(startCursor[1]-ch, text.length);
							} 
						} else {
							if (nextCh<=endCursor[1]) {
								markText(0, text.length);
							} else if (ch<endCursor[1]) {
								markText(0, endCursor[1]-ch);
							} 
						}
						ch = nextCh;
					}
				});
				if ($td.is($endTd) || $td.length == 0) {
					break;
				} else {
					if ($startTd.hasClass("left")) {
						$td = $td.parent().next().children("td.left");
					} else if ($startTd.hasClass("right")) {
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

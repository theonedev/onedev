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
			// use a timeout to make sure selection remains stable after mouse or keyboard action
	    	setTimeout(function() { 
	    		function hideSelectionPopup() {
	    			$("#selection-popup").hide();	    			
	    		}
		    	var selection = window.getSelection();
		    	if (!selection.rangeCount) {
		    		hideSelectionPopup();
		    		return;
		    	}
	    		var firstRange = selection.getRangeAt(0).cloneRange();
	    		var lastRange = selection.getRangeAt(selection.rangeCount-1).cloneRange();
	    		var $start = $(firstRange.startContainer);
	    		var $end = $(lastRange.endContainer);
	    		
	    		/* 
	    		 * offset represents offset within $start or $end node, for instance for below selection
	    		 * <span>hello</span><span>world</span>
	    		 *         ^                   ^
	    		 * $start will be <span>hello</span> and startOffset will be index of character 'l' which is 2, 
	    		 * $end will be <span>world</span> and endOffset will be index of character 'd' which will be 4  
	    		 */   
	    		var startOffset = firstRange.startOffset;
	    		var endOffset = lastRange.endOffset;
	    		
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
    			
    			// selection must be within same file
    			if ($startDiff.length == 0 || $endDiff.length == 0 || !$startDiff.is($endDiff)) { 
    				hideSelectionPopup();
    				return;
    			}
    			
				var $startTd = $start.is("td.content")? $start: $start.closest(".text-diff td.content");
				var $endTd = $end.is("td.content")? $end: $end.closest(".text-diff td.content");

				// at least one side of selection must be within a table cell
				if ($startTd.length == 0 && $endTd.length == 0) {
    				hideSelectionPopup();
    				return;
				}

				// make sure we are processing td.content on the same side on split view
				function getTd($tr) {
					var $td = $startTd.length != 0? $startTd: $endTd;
					if ($td.hasClass("left"))
						return $tr.find("td.content.left");
					else if ($td.hasClass("right"))
						return $tr.find("td.content.right");
					else
						return $tr.find("td.content");
				}

				/*
				 * sometimes the selection returns $start and $end as td instead of 
				 * children under td, for instance:
				 * <td><span>hello</span><span>world</span></td>
				 *                                        ^
				 * in this case, both $start and $end is the whole td, and startOffset
				 * is index of first span element which is 0, and endOffset is index of 
				 * last span element plus 1 which is 2. 
				 * 
				 * this function returns the equivalent child node and offset under td. 
				 * with above example, the equivalent child node will be <span>hello</span>
				 * and offset will be 0 for selection start, while equivalent end child node
				 * is <span>world</span> with offset being 5   
				 * 
				 */
				function getNodeAndOffsetUnderTd($td, contentIndex) {
    				var $contents = $td.contents();
	    			for (var i=0; i<$contents.length; i++) {
	    				if (i == contentIndex) {
	    					return {
	    						node: $($contents[i]),
	    						offset: 0
	    					}
	    				}
	    			}		    
	    			var $lastContent = $contents.last();
    				return {
    					node: $lastContent,
    					offset: $lastContent.text().length
    				}
				}

				// normalize $start to be within td.content
				if ($start.parent().parent().is("td.content")) // $start might be the text node under span
					$start = $start.parent();
				if (!$start.parent().is("td.content")) { 
					var $td;
    				if ($start.is("td.content")) { // this happens if we triple click to select a line in firefox 
    					$td = $start;
    				} else if ($start.is("tr.code")) { // this may happen if we drag mouse to select multiple lines in firefox 
    					$td = getTd($start);
    					startOffset = startOffset<=$td.index()? 0: $td.contents().length;
    				} else if ($start.is("tr.expander") || $start.closest("tr.expander").length != 0) { // this may happen if we drag mouse over to expander line 
    					var $tr = $start.is("tr.expander")? $start.next(): $start.closest("tr.expander").next();
    					if ($tr.length == 0) {
    						hideSelectionPopup();
    						return;
    					}
						$td = getTd($tr);
						startOffset = 0;
    				} else {
    					var $tr = $startDiff.find("tr.code").first();
    					if ($tr.length == 0) {
    						hideSelectionPopup();
    						return;
    					}
						$td = getTd($tr);
						startOffset = 0;
    				}
    				var nodeAndOffset = getNodeAndOffsetUnderTd($td, startOffset);
    				$start = nodeAndOffset.node;
    				startOffset = nodeAndOffset.offset;
				}
				
				// drag mouse over a line below expander, chrome may set $start as the line 
				// above expander, below code normalizes this case
				$startTd = $start.parent();
				var $lastContent = $startTd.contents().last();
				var noneSelected = $start.is($lastContent) && startOffset >= $lastContent.text().length;
				if (noneSelected && $startTd.parent().next().is("tr.expander")) {
					var $tr = $startTd.parent().next().next();
					if ($tr.length == 0) {
						hideSelectionPopup();
						return;
					}
					var $td = getTd($tr);
					$start = $td.contents().first();
					startOffset = 0;
				}
				
				// normalize $end to be within td.content
				if ($end.parent().parent().is("td.content"))
					$end = $end.parent();
				if (!$end.parent().is("td.content")) { 
					var $td;
    				if ($end.is("td.content")) {
    					$td = $end;
    				} else if ($end.is("tr.code")) {
    					$td = getTd($end);
    					endOffset = endOffset>$td.index()? $td.contents().length: 0;
    				} else if ($end.is("tr.expander") || $end.closest("tr.expander").length != 0) {
    					var $tr = $end.is("tr.expander")? $end.prev(): $end.closest("tr.expander").prev();
    					if ($tr.length == 0) {
    						hideSelectionPopup();
    						return;
    					}
						$td = getTd($tr);
						endOffset = $td.contents().length;
    				} else {
    					var $tr = $endDiff.find("tr.code").last();
    					if ($tr.length == 0) {
    						hideSelectionPopup();
    						return;
    					}
						$td = getTd($tr);
						endOffset = $td.contents().length;
    				}
    				var nodeAndOffset = getNodeAndOffsetUnderTd($td, endOffset);
    				$end = nodeAndOffset.node;
    				endOffset = nodeAndOffset.offset;
				}
				
				// drag mouse over a line above expander, chrome may set $end as the line 
				// below expander, below code normalizes this case
				$endTd = $end.parent();
				var $firstContent = $endTd.contents().first();
				var noneSelected = $end.is($firstContent) && endOffset == 0;
				if (noneSelected && $endTd.parent().prev().is("tr.expander")) {
					var $tr = $endTd.parent().prev().prev();
					if ($tr.length == 0) {
						hideSelectionPopup();
						return;
					}
					var $td = getTd($tr);
					$end = $td.contents().last();
					endOffset = $end.text().length;
				}
				
				$startTd = $start.parent();
				$endTd = $end.parent();
				
				// selection must not span the split view
				if ($startTd.hasClass("left") && $endTd.hasClass("right") 
						|| $startTd.hasClass("right") && $endTd.hasClass("left")) { 
					hideSelectionPopup();
					return;
				}	    	
				
				var $startTr = $startTd.parent();
				var $endTr = $endTd.parent();
				if ($startTr.index() > $endTr.index()) {
					hideSelectionPopup();
					return;
				}

				function nextContent($content) {
					var $td = $content.parent();
					var array = $td.contents().toArray();
					var index = array.indexOf($content[0]);
					if (index == array.length-1) {
						var $nextTr = $td.parent().next();
						if ($nextTr.hasClass("expander")) {
							$nextTr = $nextTr.next();
						}
						if ($nextTr.length == 0)
							return undefined;
						return getTd($nextTr).contents().first();
					} else {
						return $(array[index+1]);
					}
				}
				
				function prevContent($content) {
					var $td = $content.parent();
					var array = $td.contents().toArray();
					var index = array.indexOf($content[0]);
					if (index == 0) {
						var $prevTr = $td.parent().prev();
						if ($prevTr.hasClass("expander")) {
							$prevTr = $prevTr.next();
						}
						if ($prevTr.length == 0)
							return undefined;
						return getTd($prevTr).contents().last();
					} else {
						return $(array[index-1]);
					}
				}

				function hasOldOrNewData($content) {
					var $td = $content.parent();
					return $td.attr("data-old") || $td.attr("data-new");					
				}
				
				/*
				 * continue to normalize $start and $end. This time we convert below selection:
				 * <span>begin</span><span>middle</span><span>end</span>
				 *            ^                               ^       
				 * into this selection:
				 * <span>begin</span><span>middle</span><span>end</span>
				 *                         ^     ^
				 * this normalization makes it accurate to detect invalid selections where start 
				 * and end of selection points to different revisions
				 */
				var $content = $start;
				while (!$content.is($end) && (startOffset >= $content.text().length || !hasOldOrNewData($content))) {
					$content = nextContent($content);
					if (!$content) {
						hideSelectionPopup();
						return;
					} else {
						$start = $content;
						startOffset = 0;
					}
				}

				$content = $end;
				while (!$content.is($start) && (endOffset == 0 || !hasOldOrNewData($content))) {
					$content = prevContent($content);
					if (!$content) {
						hideSelectionPopup();
						return;
					} else {
						$end = $content;
						endOffset = $content.text().length;
					}
				}

				// check if there is anything selected
				if ($start.is($end) && startOffset >= endOffset 
						|| !hasOldOrNewData($start) || !hasOldOrNewData($end)) {
					hideSelectionPopup();
					return;
				}
				
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
	
				function showInvalidSelection() {
		    		var invalidSelectionUrl = "http://wiki.pmease.com/display/gp/Diff+Selection";
					var permanentCallback = function($permanentLink) {
						$permanentLink.off("click");
	    				$permanentLink.attr("href", invalidSelectionUrl);
	    				$permanentLink.html("<i class='fa fa-link'></i> No permanent link to this selection, see why");
					};
		    		var commentCallback = function($commentLink) {
	    				$commentLink.attr("href", invalidSelectionUrl);
	    				$commentLink.html("<i class='fa fa-comment'></i> Unable to comment this selection, see why");
		    		};
		    		$("#selection-popup").data("open")(position, permanentCallback, 
		    				commentCallback, $container[0]);	
				}

				// all lines between selection has been expanded
				if ($startTr.nextAll("tr.expander").filter($endTr.prevAll("tr.expander")).length != 0
						|| $startTr.prevAll("tr.expander").filter($endTr.nextAll("tr.expander")).length != 0) {  
					showInvalidSelection();
					return;
				}
				
				/*
				 * cursor.line represents line number of selection and cursor.ch represents 
				 * character index inside text of the whole line, without considering element 
				 * tags. 
				 */
	    		function getCursor($node, offset) {
					$nodeTd = $node.parent();
	    			var oldLine, newLine;
					if (!$nodeTd.hasClass("old") && !$nodeTd.hasClass("new") 
							|| $nodeTd.hasClass("old") && $nodeTd.hasClass("new")) {
						oldLine = $nodeTd.data("old") + 1;
						newLine = $nodeTd.data("new") + 1;
					} else if ($nodeTd.hasClass("old")) {
						oldLine = $nodeTd.data("old") + 1;
					} else {
						newLine = $nodeTd.data("new") + 1;
					}
					var $contents = $nodeTd.contents();
					var oldOffset = 0, newOffset = 0;
	    			for (var i=0; i<$contents.length; i++) {
	    				var $content = $($contents[i]);
	    				if ($content.is($node)) {
	    					if ($content.hasClass("delete")) { 
	    						oldCh = oldOffset + offset;
	    						newCh = undefined;
	    						newLine = undefined;
	    					} else if ($content.hasClass("insert")) {
	    						oldCh = undefined;
	    						oldLine = undefined;
	    						newCh = newOffset + offset;
	    					} else {
	    						oldCh = oldOffset + offset;
	    						newCh = newOffset + offset;
	    					}
	    					break;
	    				} else {
	    					var len = $content.text().length;
	    					if ($content.hasClass("delete")) {
	    						oldOffset += len;
	    					} else if ($content.hasClass("insert")) {
	    						newOffset += len;
	    					} else {
		    					oldOffset += len;
		    					newOffset += len;
	    					}
	    				}
	    			}
	    			if ($nodeTd.hasClass("left")) {
	    				newLine = newCh = undefined;
	    			} else if ($nodeTd.hasClass("right")) {
	    				oldLine = oldCh = undefined;
	    			}
					return {
						oldLine: oldLine,
						oldCh: oldCh,
						newLine: newLine,
						newCh: newCh
					};
	    		}

	    		var startCursor = getCursor($start, startOffset);
	    		var endCursor = getCursor($end, endOffset);

	    		var markPos;
	    		if (startCursor.newLine && endCursor.newLine) {
    				markPos = "new-" + startCursor.newLine + "." + startCursor.newCh 
    						+ "-" + endCursor.newLine + "." + endCursor.newCh;		    				    			
	    		} else if (startCursor.oldLine && endCursor.oldLine) {
    				markPos = "old-" + startCursor.oldLine + "." + startCursor.oldCh 
    						+ "-" + endCursor.oldLine + "." + endCursor.oldCh;		    				    			
	    		} else {
					showInvalidSelection();
					return;
	    		}
				var permanentCallback = function($permanentLink) {
					$permanentLink.off("click");
	    			var uri = URI(window.location.href); 
	    			var markFile = $container.data("markfile");
	    			uri.removeSearch("jump-file");
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
				};
	    		var commentCallback = function($commentLink) {
    				$commentLink.removeAttr("href");
    				$commentLink.html("<i class='fa fa-comment'></i> Add comment for this selection");
	    		};
	    		$("#selection-popup").data("open")(position, permanentCallback, 
	    				commentCallback, $container[0]);				
	    		
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
			var top = $startTd.offset().top;
			var bottom = $endTd.offset().top + $endTd.outerHeight();
			var markHeight = bottom - top;
			var windowHeight = $(window).height();
			if (windowHeight <= markHeight) {
				$(window).scrollTop(top-50);
			} else {
				$(window).scrollTop((top+bottom-windowHeight)/2);
			}
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

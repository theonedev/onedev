onedev.server.textDiff = {
	symbolClasses: ".cm-property, .cm-variable, .cm-variable-2, .cm-variable-3, .cm-def, .cm-meta, .cm-string, .cm-tag, .cm-attribute, cm-builtin, cm-qualifier",
	onDomReady: function(containerId, symbolTooltipId, oldRev, newRev, callback, blameMessageCallback,
			markRange, openComment, annotationInfo, commentContainerId, doclink) {
		var $container = $("#" + containerId);
		$container.data("commentContainerId", commentContainerId);
		$container.data("callback", callback);
		$container.data("blameMessageCallback", blameMessageCallback);
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
			if (!onedev.server.mouseState.pressed) {
				if ($(this).hasClass("left")) {
					$container.find("td.content.right").addClass("noselect");
					$container.find("td.content.left").removeClass("noselect");
				} else if ($(this).hasClass("right")) {
					$container.find("td.content.left").addClass("noselect");
					$container.find("td.content.right").removeClass("noselect");
				}
			}
		});
		var $symbols = $container.find(onedev.server.textDiff.symbolClasses); 
		$symbols.mouseover($container.data("symbolHover"));
		$container.find("td.content").mouseover($container.data("onMouseOverContent"));

		onedev.server.textDiff.initBlameTooltip(containerId, $container.find("td.blame>a.hash"));
		
		$container.selectionPopover("init", function(e) {
	    	if ($(e.target).closest("td.content").length == 0 
					|| $(e.target).closest(".selection-popover").length != 0) {
	    		return;
			}
			
	    	var selection = window.getSelection();
	    	if (!selection.rangeCount) {
	    		return "close";
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
	    		return "close";
			}
			
			var $startTd = $start.is("td.content")? $start: $start.closest(".text-diff td.content");
			var $endTd = $end.is("td.content")? $end: $end.closest(".text-diff td.content");

			// at least one side of selection must be within a table cell
			if ($startTd.length == 0 && $endTd.length == 0) {
	    		return "close";
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
			    		return "close";
					}
					$td = getTd($tr);
					startOffset = 0;
				} else {
					var $tr = $startDiff.find("tr.code").first();
					if ($tr.length == 0) {
			    		return "close";
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
		    		return "close";
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
			    		return "close";
					}
					$td = getTd($tr);
					endOffset = $td.contents().length;
				} else {
					var $tr = $endDiff.find("tr.code").last();
					if ($tr.length == 0) {
			    		return "close";
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
		    		return "close";
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
	    		return "close";
			}	    	
			
			var $startTr = $startTd.parent();
			var $endTr = $endTd.parent();
			if ($startTr.index() > $endTr.index()) {
	    		return "close";
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
		    		return "close";
				} else {
					$start = $content;
					startOffset = 0;
				}
			}

			$content = $end;
			while (!$content.is($start) && (endOffset == 0 || !hasOldOrNewData($content))) {
				$content = prevContent($content);
				if (!$content) {
		    		return "close";
				} else {
					$end = $content;
					endOffset = $content.text().length;
				}
			}

			// check if there is anything selected
			if ($start.is($end) && startOffset >= endOffset 
					|| !hasOldOrNewData($start) || !hasOldOrNewData($end)) {
	    		return "close";
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
				var $content = $("<div></div>");
				$content.append(`<a class='invalid'><svg class='icon'><use xlink:href='${onedev.server.icons}#warning'/></svg> Invalid selection, click for details</a>`);
				$content.children("a").attr("href", doclink + "/pages/diff-selection.md").attr("target", "_blank");
				return {
					position: position, 
					content: $content
				}
			}

			if ($startTr.hasClass("expanded") || $endTr.hasClass("expanded") 
					|| $startTr.nextAll("tr.expander, tr.expanded").filter($endTr.prevAll("tr.expander, tr.expanded")).length != 0
					|| $startTr.prevAll("tr.expander, tr.expanded").filter($endTr.nextAll("tr.expander, tr.expanded")).length != 0) {  
				return showInvalidSelection();
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

    		if (startCursor.newLine && endCursor.newLine) {
	    		callback("openSelectionPopover", Math.round(position.left), Math.round(position.top), false, 
	    				startCursor.newLine-1, startCursor.newCh, endCursor.newLine-1, endCursor.newCh);
    		} else if (startCursor.oldLine && endCursor.oldLine) {
	    		callback("openSelectionPopover", Math.round(position.left), Math.round(position.top), true, 
	    				startCursor.oldLine-1, startCursor.oldCh, endCursor.oldLine-1, endCursor.oldCh);
    		} else {
				return showInvalidSelection();
    		}			
		});
		
		let oldCoverages = annotationInfo.oldAnnotations.coverages;
		for (var line in oldCoverages) {
			if (oldCoverages.hasOwnProperty(line)) 
				onedev.server.textDiff.addCoverateInfo($container, true, line, oldCoverages[line]);
		}
		
		let newCoverages = annotationInfo.newAnnotations.coverages;
		for (var line in newCoverages) {
			if (newCoverages.hasOwnProperty(line)) 
				onedev.server.textDiff.addCoverateInfo($container, false, line, newCoverages[line]);
		}

		let oldProblems = annotationInfo.oldAnnotations.problems;
		for (var line in oldProblems) {			
			if (oldProblems.hasOwnProperty(line)) { 
				let oldProblemsOnLine = oldProblems[line];
				for (var i in oldProblemsOnLine) 
					oldProblemsOnLine[i].range.leftSide = true;
				onedev.server.textDiff.addProblemInfo($container, true, line, oldProblems[line]);
			}
		}
		
		let newProblems = annotationInfo.newAnnotations.problems;
		for (var line in newProblems) {
			if (newProblems.hasOwnProperty(line)) {  
				let newProblemsOnLine = newProblems[line];
				for (var i in newProblemsOnLine) 
					newProblemsOnLine[i].range.leftSide = false;
				onedev.server.textDiff.addProblemInfo($container, false, line, newProblems[line]);
			}
		}
		
		if (openComment) {
			$container.data("openComment", openComment);
		}
		var oldComments = annotationInfo.oldAnnotations.comments;
		for (var line in oldComments) {
		    if (oldComments.hasOwnProperty(line)) {
				let oldCommentsOnLine = oldComments[line];
				for (var i in oldCommentsOnLine) 
					oldCommentsOnLine[i].range.leftSide = true;
		    	onedev.server.textDiff.addCommentIndicator($container, true, line, oldCommentsOnLine);
		    }
		}
		var newComments = annotationInfo.newAnnotations.comments;
		for (var line in newComments) {
		    if (newComments.hasOwnProperty(line)) {
				let newCommentsOnLine = newComments[line];
				for (var i in newCommentsOnLine) 
					newCommentsOnLine[i].range.leftSide = false;
		    	onedev.server.textDiff.addCommentIndicator($container, false, line, newCommentsOnLine);
		    }
		}
		
		onedev.server.textDiff.highlightCommentTrigger($container);				
		
		if (markRange) {
			$container.data("markRange", markRange);
			onedev.server.textDiff.mark($container, markRange);	
		}			
	},
	onWindowLoad: function(containerId, markRange) {
		var $container = $("#" + containerId);
		if (onedev.server.viewState.getFromHistory() === undefined)
			onedev.server.textDiff.scrollTo($container, markRange);
	},
	initBlameTooltip: function(containerId, $hashLink) {
		var $container = $("#" + containerId);
		var alignment = {targetX: 100, targetY: 100, x: 0, y: 0};
		$hashLink.hover(function() {
			var $commitLink = $(this);
			var $content = $commitLink.closest("tr").children("td.content");
			var oldLine = $content.data("old");
			if (oldLine == undefined)
				oldLine = -1;
			var newLine = $content.data("new");
			if (newLine == undefined)
				newLine = -1;

			var tooltipId = "blame-message-" + containerId + "_" + oldLine + "_" + newLine;
			$container.data("blameMessageCallback")(tooltipId, $(this).data("hash"));
			var $tooltip = $("<div class='blame-message'><div class='loading'>Loading...</div></div>");
			$tooltip.attr("id", tooltipId);
			$tooltip.data("trigger", this);
			$tooltip.data("alignment", alignment);
			$container.append($tooltip);
			return $tooltip;
		}, alignment);
	},
	openSelectionPopover: function(containerId, position, markRange, markUrl, markedText, loggedIn) {
		var $container = $("#" + containerId);
		
		if (!markUrl) {
			$content = $(`<div><span class='invalid'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#warning'/></svg> Unable to comment here</a>`);
		} else {
			var $content = $(`<div><a class='permanent'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#link'/></svg> Permanent link of this selection</a>`);
			$content.children("a.permanent").attr("href", markUrl);
			$content.append(`<a class='copy-marked'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#copy'/></svg> Copy selected text to clipboard</a>`);
			var clipboard = new Clipboard(".copy-marked", {
			    text: function(trigger) {
			        return markedText;
			    }
			});		
			clipboard.on("success", function(e) {
				clipboard.destroy();
				$(".selection-popover").remove();
			});
			if (loggedIn) {
				$content.append(`<a class='comment'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#comment'/></svg> Add comment on this selection</a>`);
				$content.children("a.comment").click(function() {
					if (onedev.server.textDiff.confirmUnsavedChanges($container)) {
						$container.data("callback")("addComment", markRange.leftSide, 
								markRange.fromRow, markRange.fromColumn, markRange.toRow, markRange.toColumn);
					}
				});
			} else {
				var loginHref = $(".sign-in").attr("href");
				$content.append(`<a class='comment' href='${loginHref}'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#warning'/></svg> Login to comment on selection</a>`);
			}			
		}		
		
		$container.selectionPopover("open", {
			position: position,
			content: $content
		});
	},
	confirmUnsavedChanges: function($container) {
		return $("#"+$container.data("commentContainerId")).find("form.dirty").length == 0 
				|| confirm("There are unsaved changes, discard and continue?"); 
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
		var $symbols = $expandedTrs.find(onedev.server.textDiff.symbolClasses); 
		$symbols.mouseover($container.data("symbolHover"));
		$expandedTrs.find("td.content").mouseover($container.data("onMouseOverContent"));
		var $firstExpandedTr = $expandedTrs.first();
		
		function processBlames(index) {
			var commitHash = $firstExpandedTr.children("td.blame").eq(index).children("a.hash").data("hash");
			var prevCommitHash;
			$firstExpandedTr.prevAll().each(function() {
				if (!prevCommitHash) {
					var $tr = $(this);
					var $prevCommitLink = $tr.children("td.blame").eq(index).children("a.hash");
					if ($prevCommitLink.length != 0) {
						prevCommitHash = $prevCommitLink.data("hash");
					}
				}
			});
			if (commitHash == prevCommitHash) {
				$firstExpandedTr.children("td.blame").eq(index).html("<div class='same-as-above'>...</div>");
			}
			
			if ($nextTr.length != 0) {
				commitHash = $nextTr.children("td.blame").eq(index).children("a.hash").data("hash");
				prevCommitHash = undefined;
				$nextTr.prevAll().each(function() {
					if (!prevCommitHash) {
						var $prevCommitLink = $(this).children("td.blame").eq(index).children("a.hash");
						if ($prevCommitLink.length != 0) {
							prevCommitHash = $prevCommitLink.data("hash");
						}
					}
				});
				if (commitHash == prevCommitHash) {
					$nextTr.children("td.blame").eq(index).html("<div class='same-as-above'>...</div>");
				}
			}
		}
		if ($firstExpandedTr.children("td.blame").length == 1) {
			processBlames(0);
		} else if ($firstExpandedTr.children("td.blame").length == 2) {
			processBlames(0);
			processBlames(1);
		}
		
		onedev.server.textDiff.initBlameTooltip(containerId, $expandedTrs.find(">td.blame>a.hash"));
	},
	getMarkInfo: function($container, markRange) {
		var oldOrNew = markRange.leftSide?"old":"new";
		var startCursor = [markRange.fromRow+1, markRange.fromColumn];
		var endCursor = [markRange.toRow+1, markRange.toColumn];
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
	scrollTo: function($container, markRange) {
		var markInfo = onedev.server.textDiff.getMarkInfo($container, markRange);
		var $startTd = markInfo.startTd;
		var $endTd = markInfo.endTd;
		if ($startTd && $endTd) {
			var $scrollParent = $startTd.scrollParent();
			$scrollParent.scrollTop($startTd.offset().top - $scrollParent.offset().top + $scrollParent.scrollTop()-50);
		}
	},
	scrollIntoView: function($container, markRange) {
		var markInfo = onedev.server.textDiff.getMarkInfo($container, markRange);
		var $startTd = markInfo.startTd;
		var $endTd = markInfo.endTd;
		if ($startTd && $endTd)
			$startTd.scrollIntoView();
	},
	mark: function($container, markRangeOrMarkRanges) {
		onedev.server.textDiff.clearMark($container);

		var markRanges;		
		if (Array.isArray(markRangeOrMarkRanges)) {
			markRanges = markRangeOrMarkRanges;
		} else {
			markRanges = [];
			markRanges.push(markRangeOrMarkRanges);
		}

		for (let i in markRanges) {
			let markRange = markRanges[i];
			var markInfo = onedev.server.textDiff.getMarkInfo($container, markRange);
			var $startTd = markInfo.startTd;
			var $endTd = markInfo.endTd;
			if ($startTd && $endTd) {
				var startCursor = markInfo.startCursor;
				var endCursor = markInfo.endCursor;
				var oldOrNew = markInfo.oldOrNew;
				var $td = $startTd;
				while (true) {
					var ch = 0;
					if (!$td.hasClass("content-mark")) {
						$td.addClass("content-mark");
						$td.data("beforemark", $td.html());
					}
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
		}
	}, 
	clearMark: function($container) {
		$container.find("td.content.content-mark").each(function() {
			var $this = $(this);
			$this.html($this.data("beforemark"));
			$this.removeClass("content-mark");
			$this.removeData("beforemark");
			$container = $this.closest(".text-diff").parent();
			$this.find(onedev.server.textDiff.symbolClasses).mouseover($container.data("symbolHover"));
		});
	},
	restoreMark: function($container) {
		var markRange = $container.data("markRange");
		if (markRange) {
			onedev.server.textDiff.mark($container, markRange);
		} else {
			onedev.server.textDiff.clearMark($container);
		}
	},
	addProblemInfo: function($container, leftSide, line, problems) {
		let oldOrNew = leftSide?"old":"new";
		$container.find("." + oldOrNew + ".problem-popover[data-line='" + line + "']").remove();
		let $lineNumTd = onedev.server.textDiff.getLineNumTd($container, leftSide, line);
		
		let $trigger = $(document.createElement("a"));
		$trigger.addClass("problem-trigger");
		
		let iconInfo = onedev.server.codeProblem.getIconInfo(problems);
		$trigger.addClass(iconInfo[1]);

		$trigger.append(`<svg class='icon icon-sm'><use xlink:href='${onedev.server.icons}#${iconInfo[0]}'/></svg>`);
		
		let markRanges = [];
		for (var i in problems) 
			markRanges.push(problems[i].range);			
		
		$trigger.mouseover(function() {
			onedev.server.textDiff.mark($container, markRanges);
		}).mouseout(function() {
			onedev.server.textDiff.restoreMark($container);
		});

		$trigger.mousedown(function() {
			/* 
			 * When there are many problems, initializing popover for all of them will slow down 
		 	 * load of the source view. So we initialize popover in mouse down event
			 */
			if (!$trigger.data("popoverInited")) {
				$trigger.popover({
					html: true, 
					sanitize: false, 
					placement: "top", 
					container: $container,
					content: onedev.server.codeProblem.renderProblems(problems),
					template: `<div data-line='${line}' class='${oldOrNew} popover problem-popover'><div class='arrow'></div><div class='popover-body'></div></div>`
				}).on("shown.bs.popover", function() {
					var $currentPopover = $(`.problem-popover.${oldOrNew}[data-line='${line}']`);
					$(".popover").not($currentPopover).popover("hide");
					$currentPopover.find(".problem-content").mouseover(function() {
						onedev.server.textDiff.mark($container, problems[$(this).index()].range);
					}).mouseout(function() {
						onedev.server.textDiff.restoreMark($container);
					}).each(function() {
						var problem = problems[$(this).index()];
						$(this).children(".add-comment").click(function() {
							if (onedev.server.textDiff.confirmUnsavedChanges($container)) {
								$currentPopover.popover("hide");
								var range = problem.range;
								$container.data("callback")("addComment", leftSide, range.fromRow, range.fromColumn, 
										range.toRow, range.toColumn);
							}
						});
					});
				}).data("popoverInited", true);				
			}			
		});		
		
		$lineNumTd.prepend($trigger);
	},
	addCoverateInfo: function($container, leftSide, line, testCount) {
		var $lineNumTd = onedev.server.textDiff.getLineNumTd($container, leftSide, line);
		var cssClass;
		var title;
		if (testCount > 0) {
			cssClass = "tested";
			title = "Tested " + testCount + " times";
		} else {
			cssClass = "not-tested";
			title ="Not covered by any tests";
		}
		$lineNumTd.find(".coverage").addClass(cssClass).attr("title", title);		
	},
	addCommentIndicator: function($container, leftSide, line, comments) {
		var oldOrNew = leftSide?"old":"new";
		$container.find("." + oldOrNew + ".comment-popover[data-line='" + line + "']").remove();
		var $lineNumTd = onedev.server.textDiff.getLineNumTd($container, leftSide, line);
		
		var callback = $container.data("callback");
		
		var $indicator = $(document.createElement("a"));
		$indicator.addClass("comment-indicator");
		$indicator.data("comments", comments);
		if (comments.length != 1) {
			/* 
			 * when there are multiple comments starts with the same line, we should 
			 * display a comment indicator which will display a comment popover with 
			 * list of comment triggers upon click, user can then click one of the 
			 * trigger link to display the actual comment content  
			 */

			var updated = false;
			var content = "";
			for (var i in comments) { 
				let cssClasses = "comment-trigger";
				if (comments[i].updated) {
					cssClasses += " updated";
					updated = true;
				}
				content += `<a class='${cssClasses}' title='Click to show comment of marked text'>#${comments[i].id}</a>`;
			}

			if (updated)
				$indicator.addClass("updated");
			
			$indicator.append(`<svg class='icon'><use xlink:href='${onedev.server.icons}#comments'/></svg>`);
			
			$indicator.popover({
				html: true, 
				sanitize: false, 
				container: $container,
				placement: "auto",
				template: `<div class='${oldOrNew} popover comment-popover' data-line='${line}'><div class='arrow'></div><div class='popover-body'></div></div>`,
				content: content
			});
			$indicator.on('shown.bs.popover', function () {
				var $currentPopover = $(`.${oldOrNew}.comment-popover[data-line='${line}']`);
				$(".popover").not($currentPopover).popover("hide");
				
				$currentPopover.find("a").each(function() {
					$(this).mouseover(function() {
						var comment = comments[$(this).index()];			        						
						onedev.server.textDiff.mark($container, comment.range);
					});
					$(this).mouseout(function() {
						onedev.server.textDiff.restoreMark($container);
					});
					$(this).click(function() {
						if (!$(this).hasClass("active") && onedev.server.textDiff.confirmUnsavedChanges($container)) {
							var comment = comments[$(this).index()];			        						
							callback("openComment", comment.id, comment.range.leftSide, comment.range.fromRow, 
									comment.range.fromColumn, comment.range.toRow, comment.range.toColumn);
						}
					});
				});
				onedev.server.textDiff.highlightCommentTrigger($container);				
			});
		} else {
			var comment = comments[0];
			if (comment.updated)
				$indicator.addClass("updated");
			$indicator.addClass("comment-trigger").attr("title", "Click to show comment of marked text");
			$indicator.append("<svg class='icon'><use xlink:href='" + onedev.server.icons + "#comment'/></svg>");
			$indicator.mouseover(function() {
				onedev.server.textDiff.mark($container, comment.range);
			});
			$indicator.mouseout(function() {
				onedev.server.textDiff.restoreMark($container);
			});
			$indicator.click(function() {
				if (!$indicator.hasClass("active") && onedev.server.textDiff.confirmUnsavedChanges($container)) {
					callback("openComment", comment.id, comment.range.leftSide, comment.range.fromRow, 
							comment.range.fromColumn, comment.range.toRow, comment.range.toColumn);
				}
			});
		}
		$lineNumTd.children(".comment-indicator").remove();
		$lineNumTd.prepend($indicator);
	},	
	getLineNumTd: function($container, leftSide, line) {
		if (leftSide) {
			var $contentTd = $container.find("td.left.content[data-old='" + line + "']");
			if ($contentTd.length == 0)
				$contentTd = $container.find("td.content[data-old='" + line + "']");
			return $contentTd.prevAll("td.number").last();
		} else {
			var $contentTd = $container.find("td.right.content[data-new='" + line + "']");
			if ($contentTd.length == 0)
				$contentTd = $container.find("td.content[data-new='" + line + "']");
			return $contentTd.prevAll("td.number").first();
		}
	},
	highlightCommentTrigger: function($container) {
		$container.find(".comment-trigger").removeClass("active");
		var openComment = $container.data("openComment");
		if (openComment) {
			var line = parseInt(openComment.range.fromRow);
			var $indicator = onedev.server.textDiff.getLineNumTd($container, openComment.range.leftSide, line).children(".comment-indicator");
			if ($indicator.length != 0) {
				var comments = $indicator.data("comments");
				if (comments.length == 1) {
					$indicator.addClass("active");
				} else {
					var oldOrNew = openComment.range.leftSide?"old":"new";
					$container.find("." + oldOrNew + ".comment-popover[data-line='" + line + "'] a").each(function() {
						var comment = comments[$(this).index()];			        						
						if (comment.id == openComment.id) {
							$(this).addClass("active");
						}
					});
				}
			}
		}
	},
	onCommentAdded: function($container, comment) {
		$container.data("openComment", comment);
		$container.data("markRange", comment.range);
		
		var line = parseInt(comment.range.fromRow);		
		var leftSide = comment.range.leftSide;
		
		var $indicator = onedev.server.textDiff.getLineNumTd($container, leftSide, line).children(".comment-indicator");
		var comments;
		if ($indicator.length != 0) {
			comments = $indicator.data("comments");
		} else {
			comments = [];
		} 
		comments.push(comment);
		onedev.server.textDiff.addCommentIndicator($container, leftSide, line, comments);
		onedev.server.textDiff.highlightCommentTrigger($container);				
	},
	onCommentDeleted: function($container) {
		$(".popover").popover("hide");
		
		var openComment = $container.data("openComment");
		if (openComment) {
			$container.removeData("openComment");
			
			var line = parseInt(openComment.range.fromRow);
			var leftSide = openComment.range.leftSide;
			var $indicator = onedev.server.textDiff.getLineNumTd($container, leftSide, line).children(".comment-indicator");
			var comments = $indicator.data("comments");
			if (comments.length == 1) {
				$indicator.remove();
			} else {
				for (var i in comments) {
					var each = comments[i];
					if (each.id == openComment.id) {
						comments.splice(i, 1);
						break;
					}
				}
				onedev.server.textDiff.addCommentIndicator($container, leftSide, line, comments);
			}
			onedev.server.textDiff.highlightCommentTrigger($container);				
		}
	},
	onCommentOpened: function($container, comment) {
		$(".popover").popover("hide");
		$container.data("openComment", comment);
		$container.data("markRange", comment.range);
		onedev.server.textDiff.highlightCommentTrigger($container);
		onedev.server.textDiff.mark($container, comment.range);
		onedev.server.textDiff.scrollIntoView($container, comment.range);
	},
	onCommentClosed: function($container) {
		$(".popover").popover("hide");
		$container.removeData("openComment");
		onedev.server.textDiff.highlightCommentTrigger($container);
	},
	onAddComment: function($container, markRange) {
		$(".popover").popover("hide");
		$container.removeData("openComment");
		$container.data("markRange", markRange);
		
		$container.selectionPopover("close");
		window.getSelection().removeAllRanges();

		// continue to operate DOM in a timer to give browser a chance to 
		// clear selections
		setTimeout(function() {
			onedev.server.textDiff.highlightCommentTrigger($container);
			onedev.server.textDiff.mark($container, markRange);
			onedev.server.textDiff.scrollIntoView($container, markRange);
		}, 100);
	}
};

$(function() {
	$(document).keydown(function(e) {
		if (e.keyCode == 27) 
			$('.problem-popover').popover("hide");
	}).mouseup(function(e) {
		if ($(e.target).closest(".problem-popover, .problem-trigger").length == 0)		
			$('.problem-popover').popover("hide");
	});
});
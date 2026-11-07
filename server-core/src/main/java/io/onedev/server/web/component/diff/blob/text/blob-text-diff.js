onedev.server.blobTextDiff = {
	symbolClasses: ".cm-property, .cm-variable, .cm-variable-2, .cm-variable-3, .cm-def, .cm-meta, .cm-string, .cm-tag, .cm-attribute, cm-builtin, cm-qualifier",
	getTableBody: function($container) {
		return $container.children("table.text-diff").children("tbody");
	},
	getCodeRows: function($container) {
		return this.getTableBody($container).children("tr.code");
	},
	getSideCellSelector: function(side, line) {
		// Both cells of an unchanged split row carry both line numbers.
		const oppositeColumn = side === "old" ? "right" : "left";
		const lineAttribute = line === undefined ? `data-${side}` : `data-${side}='${line}'`;
		return `td.content:not(.${oppositeColumn})[${lineAttribute}]`;
	},
	getLineCell: function($container, leftSide, line) {
		if (line == null)
			return $();
		const cellSelector = this.getSideCellSelector(leftSide ? "old" : "new", line);
		return this.getTableBody($container).find(`>tr.code>${cellSelector}`).first();
	},
	getCodeHunks: function($container) {
		const hunks = [];
		let rows = [];
		for (const row of this.getTableBody($container).children("tr.code, tr.expander")) {
			if (row.classList.contains("expander")) {
				if (rows.length)
					hunks.push(rows);
				rows = [];
			} else {
				rows.push(row);
			}
		}
		if (rows.length)
			hunks.push(rows);
		return hunks;
	},
	onDomReady: function(containerId, symbolTooltipId, oldRev, newRev, oldPath, newPath, 
						 callback, blameMessageCallback, markRange, openComment, 
						 annotationInfo, commentContainerId, translations, symbolContext, docsite, canComment) {
		onedev.server.blobTextDiff.translations = translations;
		var $container = $("#" + containerId);
		$container.data("commentContainerId", commentContainerId);
		$container.data("callback", callback);
		$container.data("canComment", canComment);
		$container.data("oldPath", oldPath);
		$container.data("newPath", newPath);
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
			if (onedev.server.mouseState.pressed || !$(this).is(".left, .right"))
				return;
			const $rows = onedev.server.blobTextDiff.getCodeRows($container);
			const leftSide = this.classList.contains("left");
			$rows.children("td.content.right").toggleClass("noselect", leftSide);
			$rows.children("td.content.left").toggleClass("noselect", !leftSide);
		});
		const $codeRows = this.getCodeRows($container);
		$codeRows.children("td.content").mouseover($container.data("onMouseOverContent")).click(function() {
			$(".blob-text-diff>table.text-diff>tbody>tr.code>td.content.active").removeClass("active");
			$(this).addClass("active");
			callback("setActive");
		});

		this.initBlameTooltip(containerId, $codeRows.children("td.blame").children("a.hash"));
		
		$container.selectionPopover("init", function(e) {
			if ($(e.target).closest(".selection-popover, .inline-comment-row").length != 0) {
	    		return;
			}
			
	    	var selection = window.getSelection();

	    	if (!selection.rangeCount || !onedev.server.mouseState.position) {
	    		return "close";
	    	}

    		var firstRange = selection.getRangeAt(0).cloneRange();
    		var lastRange = selection.getRangeAt(selection.rangeCount-1).cloneRange();

    		var $start = $(firstRange.startContainer);
    		var $end = $(lastRange.endContainer);
			if ($start.closest(".inline-comment-row").length || $end.closest(".inline-comment-row").length)
				return "close";

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
			if ($start.hasClass("text-diff"))
				$startDiff = $start;
			else if ($start.children(".text-diff").length != 0)
				$startDiff = $start.children(".text-diff");
			else
				$startDiff = $start.closest(".text-diff");
			
			var $endDiff;
			if ($end.hasClass("text-diff"))
				$endDiff = $end;
			else if ($end.children(".text-diff").length != 0)
				$endDiff = $end.children(".text-diff");
			else
				$endDiff = $end.closest(".text-diff");
				
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
					return $tr.children("td.content.left");
				else if ($td.hasClass("right"))
					return $tr.children("td.content.right");
				else
					return $tr.children("td.content");
			}
			const diffRows = "tr.code, tr.expander";

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
					var $tr = $start.closest("tr.expander").nextAll(diffRows).first().filter("tr.code");
					if ($tr.length == 0) {
			    		return "close";
					}
					$td = getTd($tr);
					startOffset = 0;
				} else {
					var $tr = $startDiff.children("tbody").children("tr.code").first();
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
			const $nextRow = $startTd.parent().nextAll(diffRows).first();
			if (noneSelected && $nextRow.is("tr.expander")) {
				var $tr = $nextRow.nextAll(diffRows).first().filter("tr.code");
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
					var $tr = $end.closest("tr.expander").prevAll(diffRows).first().filter("tr.code");
					if ($tr.length == 0) {
			    		return "close";
					}
					$td = getTd($tr);
					endOffset = $td.contents().length;
				} else {
					var $tr = $endDiff.children("tbody").children("tr.code").last();
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
			const $previousRow = $endTd.parent().prevAll(diffRows).first();
			if (noneSelected && $previousRow.is("tr.expander")) {
				var $tr = $previousRow.prevAll(diffRows).first().filter("tr.code");
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
					var $nextTr = $td.parent().nextAll("tr.code").first();
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
					var $prevTr = $td.parent().prevAll("tr.code").first();
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

			position = onedev.server.mouseState.position;			

			function showInvalidSelection() {
				var $content = $("<div></div>");
				$content.append(`<a class='invalid'><svg class='icon'><use xlink:href='${onedev.server.icons}#warning'/></svg> ${onedev.server.blobTextDiff.translations["invalid-selection"]}</a>`);
				$content.children("a").attr("href", docsite + "/appendix/diff-selection").attr("target", "_blank");
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
				onedev.server.blobTextDiff.addCoverateInfo($container, true, line, oldCoverages[line]);
		}
		
		let newCoverages = annotationInfo.newAnnotations.coverages;
		for (var line in newCoverages) {
			if (newCoverages.hasOwnProperty(line)) 
				onedev.server.blobTextDiff.addCoverateInfo($container, false, line, newCoverages[line]);
		}

		let oldProblems = annotationInfo.oldAnnotations.problems;
		for (var line in oldProblems) {			
			if (oldProblems.hasOwnProperty(line)) { 
				let oldProblemsOnLine = oldProblems[line];
				for (var i in oldProblemsOnLine) 
					oldProblemsOnLine[i].target.location.leftSide = true;
				onedev.server.blobTextDiff.addProblemInfo($container, true, line, oldProblems[line]);
			}
		}
		
		let newProblems = annotationInfo.newAnnotations.problems;
		for (var line in newProblems) {
			if (newProblems.hasOwnProperty(line)) {  
				let newProblemsOnLine = newProblems[line];
				for (var i in newProblemsOnLine) 
					newProblemsOnLine[i].target.location.leftSide = false;
				onedev.server.blobTextDiff.addProblemInfo($container, false, line, newProblems[line]);
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
		    	onedev.server.blobTextDiff.addCommentIndicator($container, true, line, oldCommentsOnLine);
		    }
		}
		var newComments = annotationInfo.newAnnotations.comments;
		for (var line in newComments) {
		    if (newComments.hasOwnProperty(line)) {
				let newCommentsOnLine = newComments[line];
				for (var i in newCommentsOnLine) 
					newCommentsOnLine[i].range.leftSide = false;
		    	onedev.server.blobTextDiff.addCommentIndicator($container, false, line, newCommentsOnLine);
		    }
		}
		
		onedev.server.diffSymbolContext.init($container, symbolContext, translations);
		onedev.server.blobTextDiff.highlightCommentTrigger($container);				
		
		if (markRange) {
			$container.data("markRange", markRange);
			onedev.server.blobTextDiff.mark($container, markRange);	
		}	
	},
	onLoad: function(containerId, markRange) {
		var $container = $("#" + containerId);
		if (markRange && !$container.data("openComment") && onedev.server.canUseCustomScrollOnLoad())
			onedev.server.blobTextDiff.scrollTo($container, markRange);
		
		var $scrollParent = $container.scrollParent();
		if (!$scrollParent.data("onTextDiffScrollInstalled"))	{
			$scrollParent.data("onTextDiffScrollInstalled", true);
			$scrollParent.doneEvents("scroll", function() {
				$(".blob-text-diff").each(function() {
					onedev.server.blobTextDiff.highlightSyntax($(this));
				}, 100);
			});
		}		
		
		onedev.server.blobTextDiff.highlightSyntax($container);
		onedev.server.diffSymbolContext.update($container);
	},
	highlightSyntax($container) {
		const oldBlobPath = $container.data("oldPath");
		const newBlobPath = $container.data("newPath");
		const textDiff = onedev.server.textDiff;
		for (const hunk of this.getCodeHunks($container)) {
			const $rows = $(hunk);
			if (!$rows.not(".syntaxHighlighted").length)
				continue;
			const $scrollParent = $container.scrollParent();
			const top = $scrollParent.offset().top;
			const $lastRow = $rows.last();
			if ($rows.first().offset().top > top + $scrollParent.height()
					|| $lastRow.offset().top + $lastRow.outerHeight() < top)
				continue;

			const highlight = $rows.first().children("td.content").length === 2
				? textDiff.highlightSyntaxForSplitDiff : textDiff.highlightSyntaxForUnifiedDiff;
			highlight.call(textDiff, $rows, oldBlobPath, newBlobPath, () => {
				$rows.addClass("syntaxHighlighted");
				$rows.find(this.symbolClasses).mouseover($container.data("symbolHover"));
			});
		}
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
			var $tooltip = $(`<div class='blame-message'><div class='loading'>${onedev.server.blobTextDiff.translations["loading"]}</div></div>`);
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
			$content = $(`<div><span class='invalid'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#warning'/></svg> ${onedev.server.blobTextDiff.translations["unable-to-comment"]}</span></div>`);
		} else {
			var $content = $(`<div><a class='permanent'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#link'/></svg> ${onedev.server.blobTextDiff.translations["perma-link"]}</a>`);
			$content.children("a.permanent").attr("href", markUrl);
			$content.append(`<a class='copy-marked'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#copy'/></svg> ${onedev.server.blobTextDiff.translations["copy-to-clipboard"]}</a>`);
			var clipboard = new ClipboardJS(".copy-marked", {
			    text: function(trigger) {
			        return markedText;
			    }
			});		
			clipboard.on("success", function(e) {
				clipboard.destroy();
				$(".selection-popover").remove();
			});
				var explainSelectionText = onedev.server.blobTextDiff.translations["explain-selection"];
			if (explainSelectionText) {
				var svg = `<svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#question-circle'/></svg>`;
				$content.append(`<a class='explain-selection'>${svg} ${explainSelectionText}</a>`);
				$content.children("a.explain-selection").click(function() {
					$(".selection-popover").remove();
					$container.data("callback")("explainSelection", markRange.leftSide, 
							markRange.fromRow, markRange.fromColumn, markRange.toRow, markRange.toColumn);
				});
			}

			if (loggedIn) {
				$content.append(`<a class='comment'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#comment'/></svg> ${onedev.server.blobTextDiff.translations["add-selection-comment"]}</a>`);
				$content.children("a.comment").click(function() {
					if (onedev.server.blobTextDiff.confirmUnsavedChanges($container)) {
						$container.data("callback")("addComment", markRange.leftSide, 
								markRange.fromRow, markRange.fromColumn, markRange.toRow, markRange.toColumn);
					}
				});
			} else {
				var loginHref = $(".sign-in").attr("href");
				$content.append(`<a class='comment' href='${loginHref}'><svg class='icon mr-1'><use xlink:href='${onedev.server.icons}#warning'/></svg> ${onedev.server.blobTextDiff.translations["login-to-comment"]}</a>`);
			}
		}
				
		$container.selectionPopover("open", {
			position: position,
			content: $content
		});
	},
	confirmUnsavedChanges: function($container) {
		return $("#"+$container.data("commentContainerId")).find("form.dirty").length == 0 
				|| confirm(onedev.server.blobTextDiff.translations["unsaved-changes-prompt"]); 
	},
	expand: function(containerId, blockIndex, expandedHtml) {
		var $container = $("#" + containerId);
		const $tbody = this.getTableBody($container);
		$tbody.children("tr.diff-symbol-hunk").remove();
		var $expanderTr = $tbody.children("tr.expander" + blockIndex);
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
		$expandedTrs = $expandedTrs.filter("tr.code");
		$expandedTrs.children("td.content").mouseover($container.data("onMouseOverContent"));
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
		
		onedev.server.diffSymbolContext.refresh($container);
		onedev.server.blobTextDiff.initBlameTooltip(containerId, $expandedTrs.find(">td.blame>a.hash"));
		onedev.server.blobTextDiff.highlightSyntax($container);
		$tbody.children("tr.expander").children("td.expander").find("a").each(function() {
			tippy(this, {
				delay: [500, 0],
				placement: 'auto'
			});
		});
	},
	getMarkCells: function($container, {leftSide, fromRow, toRow}) {
		return [this.getLineCell($container, leftSide, fromRow), this.getLineCell($container, leftSide, toRow)];
	},
	scrollTo: function($container, markRange) {
		const [$startTd, $endTd] = this.getMarkCells($container, markRange);
		if ($startTd.length && $endTd.length) {
			var $scrollParent = $startTd.scrollParent();
			var marginTop = $startTd.closest(".text-diff").parent().prev().height() + 120;
			$scrollParent.scrollTop($startTd.offset().top - $scrollParent.offset().top + $scrollParent.scrollTop()-marginTop);
		}
	},
	scrollIntoView: function($container, markRange) {
		const [$startTd, $endTd] = this.getMarkCells($container, markRange);
		if ($startTd.length && $endTd.length)
			$startTd[0].scrollIntoViewIfNeeded();
	},
	mark: function($container, markRangeOrMarkRanges) {
		this.clearMark($container);
		const markRanges = Array.isArray(markRangeOrMarkRanges) ? markRangeOrMarkRanges : [markRangeOrMarkRanges];

		for (const markRange of markRanges) {
			const [$startTd, $endTd] = this.getMarkCells($container, markRange);
			if ($startTd.length && $endTd.length) {
				const {fromColumn, toColumn, leftSide} = markRange;
				const column = $startTd.is(".left, .right") ? (leftSide ? ".left" : ".right") : "";
				var $td = $startTd;
				while ($td.length) {
					const startsRange = $td.is($startTd);
					const endsRange = $td.is($endTd);
					let ch = 0;
					$td.addClass("content-mark");
					$td.contents().each(function() {
						const $span = $(this);
						const text = $span.text();
						function markText(from, to) {
							var left = text.substring(0, from);
							var middle = text.substring(from, to);
							var right = text.substring(to);
							
							if (left.length == 0 && right.length == 0 && $span.is("span")) {
								$span.addClass("content-mark");
							} else {
								var classes;
								if ($span.is("span"))
									classes = $span.attr("class");
								else
									classes = "";
								
								var $current = $span;
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
								$span.remove();
							}
						}
						if (leftSide ? $span.hasClass("insert") : $span.hasClass("delete")) {
							// Text from the other revision occupies no columns in this range.
							if ((!startsRange || fromColumn < ch) && (!endsRange || toColumn > ch))
								markText(0, text.length);
							return;
						}
						const from = startsRange ? Math.max(0, fromColumn - ch) : 0;
						const to = endsRange ? Math.min(text.length, toColumn - ch) : text.length;
						if (from < to)
							markText(from, to);
						ch += text.length;
					});
					if (endsRange)
						break;
					let nextRow = $td.parent()[0].nextElementSibling;
					while (nextRow && !nextRow.matches("tr.code"))
						nextRow = nextRow.nextElementSibling;
					$td = $(nextRow).children("td.content" + column);
				}			
			}
		}
	}, 
	clearMark: function($container) {
		this.getCodeRows($container).children("td.content.content-mark").each(function() {
			var $this = $(this);
			$this.removeClass("content-mark");
			$this.find("span").removeClass("content-mark");
		});
	},
	restoreMark: function($container) {
		var markRange = $container.data("markRange");
		if (markRange) {
			onedev.server.blobTextDiff.mark($container, markRange);
		} else {
			onedev.server.blobTextDiff.clearMark($container);
		}
	},
	addProblemInfo: function($container, leftSide, line, problems) {
		let oldOrNew = leftSide?"old":"new";
		$container.find("." + oldOrNew + ".problem-popover[data-line='" + line + "']").remove();
		let $lineNumTd = onedev.server.blobTextDiff.getLineNumTd($container, leftSide, line);
		
		let $trigger = $(document.createElement("a"));
		$trigger.addClass("problem-trigger");
		
		$trigger.addClass(onedev.server.codeProblem.getSeverityInfo(problems));

		$trigger.append(`<svg class='icon icon-sm'><use xlink:href='${onedev.server.icons}#exclamation-circle-o'/></svg>`);
		
		let markRanges = [];
		for (var i in problems) 
			markRanges.push(problems[i].target.location);			
		
		$trigger.mouseover(function() {
			onedev.server.blobTextDiff.mark($container, markRanges);
		}).mouseout(function() {
			onedev.server.blobTextDiff.restoreMark($container);
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
					content: onedev.server.codeProblem.renderProblems(problems, onedev.server.blobTextDiff.translations,
							$container.data("canComment")),
					template: `<div data-line='${line}' class='${oldOrNew} popover problem-popover'><div class='arrow'></div><div class='popover-body'></div></div>`
				}).on("shown.bs.popover", function() {
					var $currentPopover = $(`.problem-popover.${oldOrNew}[data-line='${line}']`);
					$(".popover").not($currentPopover).popover("hide");
					$currentPopover.find(".problem-content").mouseover(function() {
						onedev.server.blobTextDiff.mark($container, problems[$(this).index()].target.location);
					}).mouseout(function() {
						onedev.server.blobTextDiff.restoreMark($container);
					}).each(function() {
						var problem = problems[$(this).index()];
						$(this).children(".add-comment").click(function() {
							if (onedev.server.blobTextDiff.confirmUnsavedChanges($container)) {
								$currentPopover.popover("hide");
								var range = problem.target.location;
								$container.data("callback")("addComment", leftSide, range.fromRow, range.fromColumn, 
										range.toRow, range.toColumn);
							}
						});
					});
					tippy($currentPopover[0].querySelectorAll('[data-tippy-content]'), {
						delay: [500, 0],
						placement: 'auto'
					});
				}).data("popoverInited", true);				
			}			
		});		
		var $confusable = $lineNumTd.children(".confusable");
		if ($confusable.length != 0)
			$confusable.after($trigger);
		else
			$lineNumTd.prepend($trigger);
	},
	addCoverateInfo: function($container, leftSide, line, coverageStatus) {
		var $lineNumTd = onedev.server.blobTextDiff.getLineNumTd($container, leftSide, line);
		var cssClass = coverageStatus.toLowerCase();
		var title;
		if (coverageStatus == 'COVERED') 
			title = onedev.server.blobTextDiff.translations["covered-by-tests"]; 
		else if (coverageStatus == 'NOT_COVERED')
			title = onedev.server.blobTextDiff.translations["not-covered-by-any-test"];
		else  
			title = onedev.server.blobTextDiff.translations["partially-covered-by-some-tests"];
			
		$lineNumTd.find(".coverage").addClass(cssClass).attr("data-tippy-content", title);		
		tippy($lineNumTd.find(".coverage")[0], {
			delay: [500, 0],
			placement: 'auto'
		});		
	},
	addCommentIndicator: function($container, leftSide, line, comments) {
		var oldOrNew = leftSide?"old":"new";
		$container.find("." + oldOrNew + ".comment-popover[data-line='" + line + "']").remove();
		var $lineNumTd = onedev.server.blobTextDiff.getLineNumTd($container, leftSide, line);
		
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
				content += `<a class='${cssClasses}' data-tippy-content='${onedev.server.blobTextDiff.translations["show-comment"]}'>#${comments[i].id}</a>`;
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
						onedev.server.blobTextDiff.mark($container, comment.range);
					});
					$(this).mouseout(function() {
						onedev.server.blobTextDiff.restoreMark($container);
					});
					$(this).click(function() {
						if (!$(this).hasClass("active") && onedev.server.blobTextDiff.confirmUnsavedChanges($container)) {
							var comment = comments[$(this).index()];			        						
							callback("openComment", comment.id, comment.range.leftSide, comment.range.fromRow, 
									comment.range.fromColumn, comment.range.toRow, comment.range.toColumn);
						}
					});
					tippy(this, {
						delay: [500, 0],
						placement: 'auto'
					});					
				});
				onedev.server.blobTextDiff.highlightCommentTrigger($container);				
			});
		} else {
			var comment = comments[0];
			if (comment.updated)
				$indicator.addClass("updated");
			$indicator.addClass("comment-trigger").attr("data-tippy-content", onedev.server.blobTextDiff.translations["show-comment"]);
			$indicator.append("<svg class='icon'><use xlink:href='" + onedev.server.icons + "#comment'/></svg>");
			$indicator.mouseover(function() {
				onedev.server.blobTextDiff.mark($container, comment.range);
			});
			$indicator.mouseout(function() {
				onedev.server.blobTextDiff.restoreMark($container);
			});
			$indicator.click(function() {
				if (!$indicator.hasClass("active") && onedev.server.blobTextDiff.confirmUnsavedChanges($container)) {
					callback("openComment", comment.id, comment.range.leftSide, comment.range.fromRow, 
							comment.range.fromColumn, comment.range.toRow, comment.range.toColumn);
				}
			});
			tippy($indicator[0], {
				delay: [500, 0],
				placement: 'auto'
			});						
		}
		$lineNumTd.children(".comment-indicator").remove();
		$lineNumTd.prepend($indicator);
	},	
	getLineNumTd: function($container, leftSide, line) {
		const $numbers = this.getLineCell($container, leftSide, line).prevAll("td.number");
		return leftSide ? $numbers.last() : $numbers.first();
	},
	highlightCommentTrigger: function($container) {
		$container.find(".comment-trigger").removeClass("active");
		var openComment = $container.data("openComment");
		if (openComment) {
			var line = parseInt(openComment.range.fromRow);
			var $indicator = onedev.server.blobTextDiff.getLineNumTd($container, openComment.range.leftSide, line).children(".comment-indicator");
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
		
		var $indicator = onedev.server.blobTextDiff.getLineNumTd($container, leftSide, line).children(".comment-indicator");
		var comments;
		if ($indicator.length != 0) {
			comments = $indicator.data("comments");
		} else {
			comments = [];
		} 
		comments.push(comment);
		onedev.server.blobTextDiff.addCommentIndicator($container, leftSide, line, comments);
		onedev.server.blobTextDiff.highlightCommentTrigger($container);				
	},
	onCommentDeleted: function($container) {
		$(".popover").popover("hide");
		
		var openComment = $container.data("openComment");
		if (openComment) {
			$container.removeData("openComment");
			
			var line = parseInt(openComment.range.fromRow);
			var leftSide = openComment.range.leftSide;
			var $indicator = onedev.server.blobTextDiff.getLineNumTd($container, leftSide, line).children(".comment-indicator");
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
				onedev.server.blobTextDiff.addCommentIndicator($container, leftSide, line, comments);
			}
			onedev.server.blobTextDiff.highlightCommentTrigger($container);				
		}
	},
	onCommentOpened: function($container, comment) {
		$(".popover").popover("hide");
		$container.data("openComment", comment);
		$container.data("markRange", comment.range);
		onedev.server.blobTextDiff.highlightCommentTrigger($container);
		onedev.server.blobTextDiff.mark($container, comment.range);
		onedev.server.revisionDiff.scrollCommentIntoView();
	},
	onCommentClosed: function($container) {
		$(".popover").popover("hide");
		$container.removeData("openComment");
		onedev.server.blobTextDiff.highlightCommentTrigger($container);
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
			onedev.server.blobTextDiff.highlightCommentTrigger($container);
			onedev.server.blobTextDiff.mark($container, markRange);
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

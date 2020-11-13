onedev.server.sourceView = {
	onDomReady: function(filePath, fileContent, openComment, mark, symbolTooltipId, 
			revision, blameInfos, comments, callback, blameMessageCallback, 
			tabSize, lineWrapMode) {
		
		var $sourceView = $(".source-view");
		var $code = $sourceView.children(".code");

		var cm = CodeMirror($code[0], {
			value: fileContent,
			readOnly: onedev.server.util.isDevice()?"nocursor": true,
			theme: "eclipse",
			lineNumbers: true,
			lineWrapping: lineWrapMode == "Soft wrap",
			indentUnit: tabSize,
			tabSize: tabSize,
			styleActiveLine: true,
			styleSelectedText: true,
			foldGutter: true,
			matchBrackets: true,
			scrollbarStyle: "simple",
			highlightIdentifiers: {delay: 500},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});

		onedev.server.codemirror.setModeByFileName(cm, filePath);

		$sourceView.data("callback", callback);
		$sourceView.data("blameMessageCallback", blameMessageCallback);
		
	    if (mark)
	    	onedev.server.sourceView.mark(mark, false);

	    if (openComment)
			$sourceView.data("openComment", openComment);
	    
		var gutters = cm.getOption("gutters").slice();
		gutters.splice(0, 0, "CodeMirror-comments");
		cm.setOption("gutters", gutters);
		for (var line in comments) {
		    if (comments.hasOwnProperty(line)) {
		    	onedev.server.sourceView.addCommentGutter(line, comments[line][1]);
		    }
		}

		if (blameInfos) {
		    onedev.server.sourceView.blame(blameInfos);
    	}
		onedev.server.sourceView.highlightCommentTrigger();	

		onedev.server.codemirror.bindShortcuts(cm);
		onedev.server.sourceView.checkShortcutsBinding();
	    
	    $code.selectionPopover("init", function(e) {
	    	if ($(e.target).closest(".selection-popover").length != 0)
	    		return;
	    	var from = cm.getCursor("from");
	    	var to = cm.getCursor("to");
	    	if (from.line != to.line || from.ch != to.ch) {
	    		callback("openSelectionPopover", from.line, from.ch, to.line, to.ch);
	    		return;
	    	} else {
	    		return "close";
	    	}
	    });
	    
	    $code.find("textarea").addClass("readonly").addClass("no-autosize");
	    
	    $code.mouseover(function(e) {
			var node = e.target || e.srcElement, $node = $(node);
			if ($node.hasClass("cm-property") || $node.hasClass("cm-variable") || $node.hasClass("cm-variable-2") 
					|| $node.hasClass("cm-variable-3") || $node.hasClass("cm-def") || $node.hasClass("cm-meta")
					|| $node.hasClass("cm-string") || $node.hasClass("cm-tag") || $node.hasClass("cm-attribute")
					|| $node.hasClass("cm-builtin") || $node.hasClass("cm-qualifier")) {
				document.getElementById(symbolTooltipId).onMouseOverSymbol(revision, node);
			}
	    });
	    
		cm.on("scroll", function() {
			onedev.server.symboltooltip.removeTooltip(document.getElementById(symbolTooltipId));					
			onedev.server.mouseState.moved = false;					
		});
		
    	var cursorActivityTimer;
		cm.on("cursorActivity", function() {
			if (cursorActivityTimer) 
				clearTimeout(cursorActivityTimer);
			cursorActivityTimer = setTimeout(function() {
				var $outline = $(".source-view>.outline");
				if ($outline.is(":visible")) {
					var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
					var cursor = cm.getCursor();
					callback("syncOutline", cursor.line, cursor.ch);
					cm = null;
				}
			}, 500);
		});
		
		$code.on("getViewState", function() {
		    return onedev.server.codemirror.getViewState(cm);
		});
		$code.on("setViewState", function(e, viewState) {
		    return onedev.server.codemirror.setViewState(cm, viewState);
		});
		
		$code.on("resized", function() {
			setTimeout(function() {
				cm.setSize("100%", $code.height());
			});
			return false;
		});
	},
	checkShortcutsBinding() {
		if (!($(document).data("SourceViewShortcutsBinded"))) {
			$(document).data("SourceViewShortcutsBinded", true);

			/*
			 * Do not use hotkey plugin here as otherwise codemirror search will not function 
			 * properly in readonly mode
			 */
			$(document).on("keydown", function(e) {
				if ($(".modal:visible").length == 0 && !onedev.server.util.canInput(e.target) 
						&& e.keyCode == 79) {
					var $sourceView = $(".source-view");
					if ($sourceView.length != 0 && $(".outline-toggle").length != 0)
						$sourceView.data("callback")("outlineSearch");
				}
			});
		}
	},
	onWindowLoad: function(mark) {
		if (mark && onedev.server.viewState.getFromHistory() === undefined
				&& onedev.server.viewState.carryOver === undefined) {
			var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
			onedev.server.codemirror.scrollTo(cm, mark);
		}
	},
	initComment: function() {
		var $sourceView = $(".source-view");
		var $code = $sourceView.children(".code");
		var commentWidthCookieKey = "sourceView.comment.width";
		var $comment = $sourceView.children(".comment");
		var commentWidth = Cookies.get(commentWidthCookieKey);
		if (!commentWidth)
			commentWidth = $sourceView.outerWidth()/3;
		$comment.outerWidth(commentWidth);
		var $commentResizeHandle = $comment.children(".ui-resizable-handle");
		$comment.resizable({
			autoHide: false,
			handles: {"e": $commentResizeHandle},
			minWidth: 200,
			resize: function(e, ui) {
				var codeWidth = $code.outerWidth();
			    if(codeWidth < 300)
			    	$(this).resizable({maxWidth: ui.size.width});
			},
			stop: function(e, ui) {
				$(this).resizable({maxWidth: undefined});
				Cookies.set(commentWidthCookieKey, ui.size.width, {expires: Infinity});
			}
		});
	},
	exitFullScreen: function() {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		if (cm.getOption("fullScreen")) {
			cm.setOption("fullScreen", false);
		}
	},
	initOutline: function() {
		var $sourceView = $(".source-view");
		var $code = $sourceView.children(".code");
		var outlineWidthCookieKey = "sourceView.outline.width";
		var $outline = $sourceView.children(".outline");
		var outlineWidth = Cookies.get(outlineWidthCookieKey);
		if (!outlineWidth)
			outlineWidth = 300;
		$outline.outerWidth(outlineWidth);
		var $outlineResizeHandle = $outline.children(".ui-resizable-handle");
		$outline.resizable({
			autoHide: false,
			handles: {"w": $outlineResizeHandle},
			minWidth: 100,
			resize: function(e, ui) {
				var codeWidth = $code.outerWidth();
			    if(codeWidth < 300)
			    	$(this).resizable({maxWidth: ui.size.width});
			},
			stop: function(e, ui) {
				$(this).resizable({maxWidth: undefined});
				Cookies.set(outlineWidthCookieKey, ui.size.width, {expires: Infinity});
			}
		});
	},
	syncOutline: function(symbolId) {
		var $symbol = $("#" + symbolId);
		var $body = $(".source-view>.outline>.content>.body");
		$symbol.scrollIntoView();
		$body.find(".tree-content").removeClass("active");
		$symbol.addClass("active");
	},
	restoreMark: function() {
		var $sourceView = $(".source-view");
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var mark = $sourceView.data("mark");
		if (mark) 
			onedev.server.codemirror.mark(cm, mark);
		else 
			onedev.server.codemirror.clearMark(cm);
	},
	addCommentGutter: function(line, comments) {
		$(".comment-popover[data-line='" + line + "']").remove();
		
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var callback = $(".source-view").data("callback");
		
		var $gutter = $(document.createElement("div"));
		$gutter.addClass("CodeMirror-comment");
		$gutter.data("comments", comments);
		
		if (comments.length != 1) {
			/* 
			 * when there are multiple comments starts with the same line, we should 
			 * display a comment indicator which will display a comment popover with 
			 * list of comment triggers upon click, user can then click one of the 
			 * trigger link to display the actual comment content  
			 */
			$gutter.append("<a><svg class='icon'><use xlink:href='" + onedev.server.icons + "#comments'/></svg></a>");
			var $indicator = $gutter.children("a");
			var content = "";
			for (var i in comments) {
				var comment = comments[i];
				var index = parseInt(i) + 1;
				content += "<a class='comment-trigger' title='Click to show comment of marked text'>#" + comment.id + "</a>";
			}
			$indicator.popover({
				html: true, 
				container: ".source-view>.code",
				sanitize: false,
				placement: "auto",
				template: "<div data-line='" + line + "' class='popover comment-popover'><div class='arrow'></div><div class='popover-body'></div></div>",
				content: content
			});
			$indicator.on('shown.bs.popover', function () {
				$(".comment-popover[data-line='" + line + "'] a").each(function() {
					$(this).mouseover(function() {
						var comment = comments[$(this).index()];
						onedev.server.codemirror.mark(cm, comment.mark);
					});
					$(this).mouseout(function() {
						onedev.server.sourceView.restoreMark();
					});
					$(this).click(function() {
						if (!$(this).hasClass("active")) {
	    					if ($(".source-view form.dirty").length != 0 
	    							&& !confirm("There are unsaved changes, discard and continue?")) {
	    						return;
	    					}
							var comment = comments[$(this).index()];			        						
							callback("openComment", comment.id);
						}
					});
				});
				onedev.server.sourceView.highlightCommentTrigger();				
			});
		} else {
			var comment = comments[0];
			$gutter.append("<a class='comment-trigger' title='Click to show comment of marked text'><svg class='icon'><use xlink:href='" + onedev.server.icons + "#comment'/></svg></a>");
			var $indicator = $gutter.children("a");
			$indicator.mouseover(function() {
				onedev.server.codemirror.mark(cm, comment.mark);
			});
			$indicator.mouseout(function() {
				onedev.server.sourceView.restoreMark();
			});
			$indicator.click(function() {
				if (!$indicator.hasClass("active")) {
					if ($(".source-view form.dirty").length != 0 
							&& !confirm("There are unsaved changes, discard and continue?")) {
						return;
					}
					callback("openComment", comment.id);
				}
			});
		}
		cm.setGutterMarker(parseInt(line), "CodeMirror-comments", $gutter[0]);		
	},
	openSelectionPopover: function(mark, markUrl, loggedIn, unableCommentMessage) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;	
		var ch = (mark.fromColumn + mark.toColumn)/2;
		var position = cm.charCoords({line:mark.fromRow, ch:ch});
		
		var $content;
		if (unableCommentMessage) {
			$content = $("<div><span class='invalid'><svg class='icon mr-1'><use xlink:href='" + onedev.server.icons + "#warning'/></svg> " + unableCommentMessage + "</a>");
		} else {
			$content = $("<div><a class='permanent'><svg class='icon mr-1'><use xlink:href='" + onedev.server.icons + "#link'/></svg> Permanent link of this selection</a>");
			$content.children("a.permanent").attr("href", markUrl);
			$content.append("<a class='copy-marked'><svg class='icon mr-1'><use xlink:href='" + onedev.server.icons + "#copy'/></svg> Copy selected text to clipboard</a>");
			var clipboard = new Clipboard(".copy-marked", {
			    text: function(trigger) {
			        return cm.getSelection("\n");
			    }
			});		
			clipboard.on("success", function(e) {
				clipboard.destroy();
			});
			$content.children("a.copy-marked").click(function() {
				$(".selection-popover").remove();
			});
			if (loggedIn) {
				$content.append("<a class='comment'><svg class='icon mr-1'><use xlink:href='" + onedev.server.icons + "#comment'/></svg> Add comment on this selection</a>");
				$content.children("a.comment").click(function() {
					if ($(".source-view").find("form.dirty").length != 0 
							&& !confirm("There are unsaved changes, discard and continue?")) {
						return;
					}
					$(".selection-popover").remove();
					var callback = $(".source-view").data("callback");
					callback("addComment", mark.fromRow, mark.fromColumn, mark.toRow, mark.toColumn);
				});
			} else {
				var loginHref = $(".sign-in").attr("href");
				$content.append("<a href='" + loginHref + "' class='comment'><svg class='icon mr-1'><use xlink:href='" + onedev.server.icons + "#warning'/></svg> Login to comment on selection</a>");
			}			
		}
		
		$(".source-view>.code").selectionPopover("open", {
			position: position,
			content: $content
		});
	},
	onCommentAdded: function(comment) {
		var $sourceView = $(".source-view");
		$sourceView.data("openComment", comment);
		$sourceView.data("mark", comment.mark);
		
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var line = parseInt(comment.mark.fromRow);		
		var lineInfo = cm.lineInfo(line);
		var gutter;
		if (lineInfo.gutterMarkers)
			gutter = lineInfo.gutterMarkers["CodeMirror-comments"];
		var comments;
		if (gutter) {
			comments = $(gutter).data("comments");
		} else {
			comments = [];
		} 
		comments.push(comment);
		onedev.server.sourceView.addCommentGutter(line, comments);
		onedev.server.sourceView.highlightCommentTrigger();				
		$(window).resize();
	},
	onCommentDeleted: function(comment) {
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		
		var line = parseInt(comment.mark.fromRow);
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var lineInfo = cm.lineInfo(line);
		var $gutter = $(lineInfo.gutterMarkers["CodeMirror-comments"]);
		var comments = $gutter.data("comments");
		if (comments.length == 1) {
			cm.setGutterMarker(line, "CodeMirror-comments", null);
		} else {
			for (var i in comments) {
				if (comments[i].id == comment.id) {
					comments.splice(i, 1);
					break;
				}
			}
			onedev.server.sourceView.addCommentGutter(line, comments);
		}
		onedev.server.sourceView.highlightCommentTrigger();				
		$(window).resize();
		onedev.server.sourceView.clearMark();
	},
	onAddComment: function(mark) {
		onedev.server.sourceView.exitFullScreen();
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		$sourceView.data("mark", mark);
		$(".source-view>.code").selectionPopover("close");

		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		onedev.server.codemirror.clearSelection(cm);
		$(window).resize();

		onedev.server.sourceView.mark(mark, false);
		onedev.server.codemirror.scrollIntoView(cm, mark);
		
		onedev.server.sourceView.highlightCommentTrigger();		
		var $textarea = $sourceView.find(".comment textarea");
		$textarea.caret($textarea.val().length);		
	},
	onOpenComment: function(comment) {
		onedev.server.sourceView.exitFullScreen();
		var $sourceView = $(".source-view");
		$sourceView.data("openComment", comment);
		$sourceView.data("mark", comment.mark);
		onedev.server.sourceView.highlightCommentTrigger();

		$(window).resize();
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		onedev.server.codemirror.scrollIntoView(cm, comment.mark);
	},
	onCloseComment: function() {
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		onedev.server.sourceView.highlightCommentTrigger();
		$(window).resize();
		onedev.server.sourceView.clearMark();
	},
	onToggleOutline: function() {
		onedev.server.sourceView.exitFullScreen();
		$(".outline-toggle").prop("checked", $(".source-view>.outline").is(":visible"));
		$(window).resize();
	},
	highlightCommentTrigger: function() {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;
		$(".comment-popover a").removeClass("active");
		
		/*
		 * we can not simply select all comment triggers via jQuery 
		 * as comment gutter may be create/destroy dynamically by 
		 * CodeMirror 
		 */
		for (var i=0; i<cm.lineCount(); i++) {
			var gutterMarkers = cm.lineInfo(i).gutterMarkers;
			if (gutterMarkers) {
				var gutter = gutterMarkers["CodeMirror-comments"];
				if (gutter) {
					$(gutter).children("a").removeClass("active");
				}
			}
		}
		
		var openComment = $(".source-view").data("openComment");
		if (openComment) {
			var line = parseInt(openComment.mark.fromRow);
			var lineInfo = cm.lineInfo(line);
			if (lineInfo && lineInfo.gutterMarkers) {
				var gutter = lineInfo.gutterMarkers["CodeMirror-comments"];
				if (gutter) {
					var comments = $(gutter).data("comments");
					if (comments.length == 1) {
						$(gutter).children("a").addClass("active");
					} else {
						$(".comment-popover[data-line='" + line + "'] a").each(function() {
							var comment = comments[$(this).index()];			        						
							if (comment.id == openComment.id) {
								$(this).addClass("active");
							}
						});
					}
				}
			}
		}
	},
	mark: function(mark, scroll) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		$(".source-view").data("mark", mark);
		onedev.server.codemirror.mark(cm, mark);
		if (scroll)
			onedev.server.codemirror.scrollTo(cm, mark);
		cm.setCursor({line: mark.fromRow, ch: mark.fromColumn});
	},
	clearMark: function() {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var mark = $(".source-view").data("mark");
		if (mark) 
			onedev.server.codemirror.scrollIntoView(cm, mark);
		$(".source-view").removeData("mark");
		onedev.server.codemirror.clearMark(cm);			
	},
	blame: function(blameInfos) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var alignment = {targetX: 100, targetY: 100, x: 0, y: 0};
		
		if (blameInfos) {
			var blameMessageCallback = $(".source-view").data("blameMessageCallback");
			var gutters = cm.getOption("gutters").slice();
			gutters.splice(1, 0, "CodeMirror-annotations");
			cm.setOption("gutters", gutters);
    		for (var i in blameInfos) {
    			var blameInfo = blameInfos[i];
        		for (var j in blameInfo.ranges) {
        			var range = blameInfo.ranges[j];
        			var $gutter = $(document.createElement("div"));
        			$gutter.addClass("CodeMirror-annotation");
            		$("<a class='hash'>" + blameInfo.abbreviatedHash + "</a>")
            				.appendTo($gutter)
            				.attr("href", blameInfo.url)
            				.attr("onclick", "onedev.server.viewState.getFromViewAndSetToHistory();");
            		var $hashLink = $gutter.children("a.hash");
            		$hashLink.data("hash", blameInfo.hash);
            		$hashLink.data("line", range.from);

    				$hashLink.hover(function() {
    					var tooltipId = "blame-message-line-" + $(this).data("line");
    					blameMessageCallback(tooltipId, $(this).data("hash"));
    					var $tooltip = $("<div class='blame-message'><div class='loading'>Loading...</div></div>");
    					$tooltip.attr("id", tooltipId);
    					$tooltip.data("trigger", this);
    					$tooltip.data("alignment", alignment);
    					$(".source-view").append($tooltip);
    					return $tooltip;
    				}, alignment);
            		
            		$gutter.append("<span class='date'>" + blameInfo.commitDate + "</span>");
            		$gutter.append("<span class='author'>" + blameInfo.authorName + "</span>");
            		cm.setGutterMarker(range.from, "CodeMirror-annotations", $gutter[0]);
            		
            		for (var line = range.from+1; line<=range.to; line++) {
            			var $gutter = $(document.createElement("div"));
            			$gutter.addClass("CodeMirror-annotation");
            			$gutter.append("<span class='same-as-above'>...</span>");
                		cm.setGutterMarker(line, "CodeMirror-annotations", $gutter[0]);
            		}
        		}
    		} 
			$(".CodeMirror-annotations").addClass("need-width");
		} else {
			cm.clearGutter("CodeMirror-annotations");
			var gutters = cm.getOption("gutters").slice();
			gutters.splice(1, 1);
			cm.setOption("gutters", gutters);
		}
	},
	onLineWrapModeChange: function(lineWrapMode) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		cm.setOption("lineWrapping", lineWrapMode == "Soft wrap");
	},
	onTabSizeChange: function(tabSize) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		cm.setOption("tabSize", tabSize);
	},
	onOutlineSearchDomReady: function(containerId, callback) {
		var $body = $("#" + containerId + ">.outline-search>.modal-body");
		
		var $input = $body.children("input");
		
		$input.doneEvents("inputchange", function() {
			callback("input", $(this).val());
		}, 100);

		function onReturn() {
			if (onedev.server.form.confirmLeave()) {
				var $result = $body.children(".result");
				var $active = $result.find("a.active");
				if ($active.length != 0) {
					callback("return", $active.data("symbolindex"));
				}
			}
		}
		
		function onKeyup(e) {
			e.preventDefault();
			var $result = $body.children(".result");
			var $active = $result.find("a.active");
			var $selectables = $result.find("a.selectable");
			var index = $selectables.index($active);
			if (index > 0) {
				index--;
				var $prev = $selectables.eq(index);
				$active.removeClass("active");
				$prev.addClass("active");
				$result.find("a.active").scrollIntoView();
			}
		};
		
		function onKeydown(e) {
			e.preventDefault();
			var $result = $body.children(".result");
			var $active = $result.find("a.active");
			var $selectables = $result.find("a.selectable");
			var index = $selectables.index($active);
			if (index < $selectables.length-1) {
				index++;
				var $next = $selectables.eq(index);
				$active.removeClass("active");
				$next.addClass("active");
				$result.find("a.active").scrollIntoView();
			}
		};
		
		$body.children().bind("keydown", "return", onReturn);
		$body.children().bind("keydown", "up", onKeyup);
		$body.children().bind("keydown", "down", onKeydown);		
	}
};

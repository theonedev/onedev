turbodev.server.sourceView = {
	onDomReady: function(filePath, fileContent, openComment, mark, symbolTooltipId, 
			revision, blameInfos, comments, callback, blameMessageCallback, 
			tabSize, lineWrapMode) {
		
		var $sourceView = $(".source-view");
		var $code = $sourceView.children(".code");

		var cm = CodeMirror($code[0], {
			value: fileContent,
			// do not use "readOnly: true" here as otherwise CodeMirror will eat key input
			// and the search dialog can not be brought out via shortcuts
			readOnly: "nocursor", 
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
		
		turbodev.server.codemirror.setMode(cm, filePath);

		$sourceView.data("callback", callback);
		$sourceView.data("blameMessageCallback", blameMessageCallback);
		
	    if (mark) {
	    	turbodev.server.codemirror.mark(cm, mark);
			$sourceView.data("mark", mark);
			cm.setCursor({line: mark.beginLine, ch: 0});
	    }

	    if (openComment)
			$sourceView.data("openComment", openComment);
	    
		var gutters = cm.getOption("gutters").slice();
		gutters.splice(0, 0, "CodeMirror-comments");
		cm.setOption("gutters", gutters);
		for (var line in comments) {
		    if (comments.hasOwnProperty(line)) {
		    	turbodev.server.sourceView.addCommentGutter(line, comments[line][1]);
		    }
		}

		if (blameInfos) {
	    	turbodev.server.sourceView.blame(blameInfos);
    	}
		turbodev.server.sourceView.highlightCommentTrigger();	

		turbodev.server.codemirror.bindShortcuts(cm);

		if (!($(document).data("SourceViewShortcutsBinded"))) {
			$(document).data("SourceViewShortcutsBinded", true);

			$(document).bind("keydown", "o", function(e) {
				if ($(".modal:visible").length == 0) {
					e.preventDefault();
					var $sourceView = $(".source-view");
					if ($sourceView.length != 0 && $(".outline-toggle").length != 0)
						$sourceView.data("callback")("outlineSearch");
				}
			});
		}
	    
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
			turbodev.server.symboltooltip.removeTooltip(document.getElementById(symbolTooltipId));					
			turbodev.server.mouseState.moved = false;					
			repositionCommentPopovers();					
		});
		
    	var cursorActivityTimer;
		cm.on("cursorActivity", function() {
			if (cursorActivityTimer) 
				clearTimeout(cursorActivityTimer);
			cursorActivityTimer = setTimeout(function() {
				var $outline = $sourceView.children(".outline");
				if ($outline.is(":visible")) {
					var cursor = cm.getCursor();
					callback("syncOutline", cursor.line, cursor.ch);
				}
			}, 500);
		});
		
		$sourceView.on("getViewState", function() {
		    return turbodev.server.codemirror.getViewState(cm);
		});
		$sourceView.on("setViewState", function(e, viewState) {
		    return turbodev.server.codemirror.setViewState(cm, viewState);
		});
		
		$sourceView.on("autofit", function(event, width, height) {
			if (cm.getOption("fullScreen"))
				return;
			
			$sourceView.outerWidth(width);
			$sourceView.outerHeight(height);
			$code.outerHeight($sourceView.height());
			
			var $comment = $sourceView.children(".comment");
			var paddingLeft;
			if ($comment.is(":visible")) {
				paddingLeft = $comment.outerWidth();
				$comment.children(".ui-resizable-handle").outerHeight($sourceView.height());				

				var $commentHead = $comment.find(">.content>.head");
				var $commentBody = $comment.find(">.content>.body");
				$commentBody.outerHeight($sourceView.height() - $commentHead.outerHeight());
			} else {
				paddingLeft = 0;
			}
			$sourceView.css("padding-left", paddingLeft);
			
			var $outline = $sourceView.children(".outline");
			if ($outline.is(":visible")) {
				$sourceView.css("padding-right", $outline.outerWidth());
				$outline.css("left", $code.outerWidth() + paddingLeft);
				$outline.children(".ui-resizable-handle").outerHeight($sourceView.height());				

				var $outlineHead = $outline.find(">.content>.head");
				var $outlineBody = $outline.find(">.content>.body");
				$outlineBody.outerHeight($sourceView.height() - $outlineHead.outerHeight());
			} else {
				$sourceView.css("padding-right", "0");
			}
			
			cm.setSize($code.width(), $code.height());
			repositionCommentPopovers();
		});
		
		function repositionCommentPopovers() {
			$(".comment-popover:visible").each(function() {
				var $popover = $(this);
				var lineInfo = cm.lineInfo($popover.data("line"));
				if (lineInfo.gutterMarkers) {
					var gutter = lineInfo.gutterMarkers["CodeMirror-comments"];
					if (gutter) {
						var $indicator = $(gutter).children("a");
						if ($indicator.offset().top+$indicator.height() < $sourceView.offset().top 
								|| $indicator.offset().top > $sourceView.offset().top+$sourceView.height()) {
							$popover.css("left", "-10000px");
						} else {
							$popover.css({
								"left": $indicator.offset().left + $indicator.outerWidth() - $sourceView.offset().left,
								"top": $indicator.offset().top + ($indicator.outerHeight() - $popover.outerHeight())/2 - $sourceView.offset().top
							});
						}
					}
				}
			});
		}
	},
	onWindowLoad: function(mark) {
		if (mark && turbodev.server.viewState.getFromHistory() === undefined
				&& turbodev.server.viewState.carryOver === undefined) {
			var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
			turbodev.server.codemirror.scrollTo(cm, mark);
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
			outlineWidth = 350;
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
		$body.scrollIntoView($symbol, 20, 20);
		$body.find(".tree-content").removeClass("active");
		$symbol.addClass("active");
		
		$(window).resize();
	},
	restoreMark: function() {
		var $sourceView = $(".source-view");
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var mark = $sourceView.data("mark");
		if (mark) {
			turbodev.server.codemirror.mark(cm, mark);
		} else {
			turbodev.server.codemirror.clearMark(cm);
		}
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
			$gutter.append("<a><i class='fa fa-comments'></i></a>");
			var $indicator = $gutter.children("a");
			var content = "";
			for (var i in comments) {
				var comment = comments[i];
				var index = parseInt(i) + 1;
				content += "<a class='comment-trigger' title='Click to show details of this comment'>#" + comment.id + "</a>";
			}
			$indicator.popover({
				html: true, 
				container: ".source-view",
				placement: "right auto",
				template: "<div data-line='" + line + "' class='popover comment-popover'><div class='arrow'></div><div class='popover-content'></div></div>",
				content: content
			});
			$indicator.on('shown.bs.popover', function () {
				$(".comment-popover[data-line='" + line + "'] a").each(function() {
					$(this).mouseover(function() {
						var comment = comments[$(this).index()];			        						
						turbodev.server.codemirror.mark(cm, comment.mark);
					});
					$(this).mouseout(function() {
						turbodev.server.sourceView.restoreMark();
					});
					$(this).click(function() {
    					if ($(".source-view form.dirty").length != 0 
    							&& !confirm("There are unsaved changes, discard and continue?")) {
    						return;
    					}
						var comment = comments[$(this).index()];			        						
						callback("toggleComment", comment.id);
					});
				});
				turbodev.server.sourceView.highlightCommentTrigger();				
			});
		} else {
			var comment = comments[0];
			$gutter.append("<a class='comment-trigger' title='Click to show comment of marked text'><i class='fa fa-commenting'></i></a>");
			var $indicator = $gutter.children("a");
			$indicator.mouseover(function() {
				turbodev.server.codemirror.mark(cm, comment.mark);
			});
			$indicator.mouseout(function() {
				turbodev.server.sourceView.restoreMark();
			});
			$indicator.click(function() {
				if ($(".source-view form.dirty").length != 0 
						&& !confirm("There are unsaved changes, discard and continue?")) {
					return;
				}
				callback("toggleComment", comment.id);
			});
		}
		cm.setGutterMarker(parseInt(line), "CodeMirror-comments", $gutter[0]);		
	},
	openSelectionPopover: function(mark, markUrl, loggedIn, unableCommentMessage) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;	
		var ch = (mark.beginChar + mark.endChar)/2;
		var position = cm.charCoords({line:mark.beginLine, ch:ch});
		
		var $content;
		if (unableCommentMessage) {
			$content = $("<div><span class='invalid'><i class='fa fa-warning'></i> " + unableCommentMessage + "</a>");
		} else {
			$content = $("<div><a class='permanent'><i class='fa fa-link'></i> Permanent link of this selection</a>");
			$content.children("a.permanent").attr("href", markUrl);
			$content.append("<a class='copy-marked'><i class='fa fa-clipboard'></i> Copy selected text to clipboard</a>");
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
				$content.append("<a class='comment'><i class='fa fa-comment'></i> Add comment on this selection</a>");
				$content.children("a.comment").click(function() {
					if ($(".source-view").find("form.dirty").length != 0 
							&& !confirm("There are unsaved changes, discard and continue?")) {
						return;
					}
					$(".selection-popover").remove();
					var callback = $(".source-view").data("callback");
					callback("addComment", mark.beginLine, mark.beginChar, mark.endLine, mark.endChar);
				});
			} else {
				$content.append("<span class='comment'><i class='fa fa-warning'></i> Login to comment on selection</span>");
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
		var line = parseInt(comment.mark.beginLine);		
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
		turbodev.server.sourceView.addCommentGutter(line, comments);
		turbodev.server.sourceView.highlightCommentTrigger();				
		turbodev.server.sourceView.onLayoutChange();
	},
	onCommentDeleted: function(comment) {
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		
		var line = parseInt(comment.mark.beginLine);
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
			turbodev.server.sourceView.addCommentGutter(line, comments);
		}
		turbodev.server.sourceView.highlightCommentTrigger();				
		turbodev.server.sourceView.onLayoutChange();
		
		turbodev.server.sourceView.mark(undefined);
	},
	onLayoutChange: function() {
		$sourceView = $('.source-view');
		$sourceView.triggerHandler('autofit', [$sourceView.outerWidth(), $sourceView.outerHeight()]);
	},
	onAddComment: function(mark) {
		turbodev.server.sourceView.exitFullScreen();
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		$sourceView.data("mark", mark);
		$(".source-view>.code").selectionPopover("close");

		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		turbodev.server.codemirror.clearSelection(cm);
		turbodev.server.sourceView.onLayoutChange();

		// Mark again to make sure marked text still exists in viewport after layout change
		turbodev.server.sourceView.mark(mark);
		
		turbodev.server.sourceView.highlightCommentTrigger();		
		var $textarea = $sourceView.find(".comment textarea");
		$textarea.caret($textarea.val().length);		
	},
	onOpenComment: function(comment) {
		turbodev.server.sourceView.exitFullScreen();
		var $sourceView = $(".source-view");
		$sourceView.data("openComment", comment);
		$sourceView.data("mark", comment.mark);
		turbodev.server.sourceView.highlightCommentTrigger();
		turbodev.server.sourceView.onLayoutChange();
		
		// Mark again to make sure marked text still exists in viewport after layout change
		turbodev.server.sourceView.mark(comment.mark);
	},
	onCloseComment: function() {
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		turbodev.server.sourceView.highlightCommentTrigger();
		turbodev.server.sourceView.onLayoutChange();
		turbodev.server.sourceView.mark(undefined);
	},
	onToggleOutline: function() {
		turbodev.server.sourceView.exitFullScreen();
		turbodev.server.sourceView.onLayoutChange();
		$(".outline-toggle").prop("checked", $(".source-view>.outline").is(":visible"));
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
			var line = parseInt(openComment.mark.beginLine);
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
	mark: function(mark) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		if (mark) {
			$(".source-view").data("mark", mark);
			turbodev.server.codemirror.mark(cm, mark);
			turbodev.server.codemirror.scrollTo(cm, mark);
			cm.setCursor({line: mark.beginLine, ch: 0});
		} else {
			$(".source-view").removeData("mark");
			turbodev.server.codemirror.clearMark(cm);			
		}
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
            				.attr("onclick", "turbodev.server.viewState.getFromViewAndSetToHistory();");
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
			if (turbodev.server.form.confirmLeave()) {
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
				$result.scrollIntoView("a.active", 36, 36);
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
				$result.scrollIntoView("a.active", 36, 36);
			}
		};
		
		$body.children().bind("keydown", "return", onReturn);
		$body.children().bind("keydown", "up", onKeyup);
		$body.children().bind("keydown", "down", onKeydown);		
	}
};

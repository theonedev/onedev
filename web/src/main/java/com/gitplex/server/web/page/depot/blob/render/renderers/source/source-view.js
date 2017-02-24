gitplex.server.sourceView = {
	init: function(filePath, fileContent, openComment, mark, symbolTooltipId, 
			revision, blameInfos, comments, callback, blameMessageCallback, 
			loggedIn, anchor, tabSize, lineWrapMode) {
		
		var $sourceView = $(".source-view");
		var $code = $sourceView.children(".code");
		var options = {
			value: fileContent,
			readOnly: gitplex.server.isDevice()?"nocursor":true,
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
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
			extraKeys: {
				"F11": function(cm) {
					cm.setOption("fullScreen", !cm.getOption("fullScreen"));
				},
				"Esc": function(cm) {
					if (cm.getOption("fullScreen")) {
						cm.setOption("fullScreen", false);
						$(window).resize();
					}
		        }
			}
		};

		var cm = CodeMirror($code[0], options);
		gitplex.server.codemirror.setMode(cm, filePath);

		$sourceView.data("callback", callback);
		$sourceView.data("blameMessageCallback", blameMessageCallback);
		
		// hide scroll bar of parent scrollable as we will be using 
		// CodeMirror scroll bar
		$sourceView.closest(".scrollable").css("overflow", "visible");
		
	    if (mark) {
	    	gitplex.server.codemirror.mark(cm, mark, true);
			$sourceView.data("mark", mark);
	    }

	    if (openComment)
			$sourceView.data("openComment", openComment);
	    
		if (anchor) {
			var $anchor = $("#"+anchor);
			if ($anchor.length != 0) {
				setTimeout(function() {
					$anchor.closest(".code-comment").parent().scrollIntoView($anchor);
				}, 0);
			}
		} 
		
    	// add gutters with a timer to avoid the issue that occasionally 
    	// gutter becomes much wider than expected
	    setTimeout(function() {
			var gutters = cm.getOption("gutters").slice();
			gutters.splice(0, 0, "CodeMirror-comments");
			cm.setOption("gutters", gutters);
			for (var line in comments) {
			    if (comments.hasOwnProperty(line)) {
			    	gitplex.server.sourceView.addCommentGutter(line, comments[line][1]);
			    }
			}

			if (blameInfos) {
		    	gitplex.server.sourceView.blame(blameInfos);
	    	}
			gitplex.server.sourceView.highlightCommentTrigger();				
	    }, 10);
	    
	    $code.selectionPopover("init", function() {
	    	if (cm.hasFocus()) {
		    	var from = cm.getCursor("from");
		    	var to = cm.getCursor("to");
		    	if (from.line != to.line || from.ch != to.ch) {
		    		callback("openSelectionPopover", from.line, from.ch, to.line, to.ch);
		    		return;
		    	}
	    	}	
	    	return "close";
	    });
	    
	    $code.mouseover(function(e) {
			var node = e.target || e.srcElement, $node = $(node);
			if ($node.hasClass("cm-property") || $node.hasClass("cm-variable") || $node.hasClass("cm-variable-2") 
					|| $node.hasClass("cm-variable-3") || $node.hasClass("cm-def") || $node.hasClass("cm-meta")
					|| $node.hasClass("cm-string") || $node.hasClass("cm-tag") || $node.hasClass("cm-attribute")) {
				document.getElementById(symbolTooltipId).onMouseOverSymbol(revision, node);
			}
	    });
	    
		cm.on("scroll", function() {
			gitplex.server.symboltooltip.removeTooltip(document.getElementById(symbolTooltipId));					
			gitplex.server.mouseState.moved = false;					
			repositionCommentPopovers();					
		});
			
		$sourceView.on("getViewState", function() {
		    return gitplex.server.codemirror.getViewState(cm);
		});
		$sourceView.on("setViewState", function(e, viewState) {
		    return gitplex.server.codemirror.setViewState(cm, viewState);
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
	initComment: function() {
		var $sourceView = $(".source-view");
		var $code = $sourceView.children(".code");
		var commentWidthCookieKey = "sourceView.comment.width";
		var $comment = $sourceView.children(".comment");
		var commentWidth = Cookies.get(commentWidthCookieKey);
		if (!commentWidth)
			commentWidth = 400;
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
	restoreMark: function() {
		var $sourceView = $(".source-view");
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var mark = $sourceView.data("mark");
		if (mark) {
			gitplex.server.codemirror.mark(cm, mark, false);
		} else {
			gitplex.server.codemirror.clearMark(cm);
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
						gitplex.server.codemirror.mark(cm, comment.mark, false);
					});
					$(this).mouseout(function() {
						gitplex.server.sourceView.restoreMark();
					});
					$(this).click(function() {
    					if ($(".source-view form.dirty").length != 0 
    							&& !confirm("There are unsaved changes, discard and continue?")) {
    						return;
    					}
						var comment = comments[$(this).index()];			        						
						callback("openComment", comment.id);
					});
				});
				gitplex.server.sourceView.highlightCommentTrigger();				
			});
		} else {
			var comment = comments[0];
			$gutter.append("<a class='comment-trigger' title='Click to show comment of marked text'><i class='fa fa-commenting'></i></a>");
			var $indicator = $gutter.children("a");
			$indicator.mouseover(function() {
				gitplex.server.codemirror.mark(cm, comment.mark, false);
			});
			$indicator.mouseout(function() {
				gitplex.server.sourceView.restoreMark();
			});
			$indicator.click(function() {
				if ($(".source-view form.dirty").length != 0 
						&& !confirm("There are unsaved changes, discard and continue?")) {
					return;
				}
				callback("openComment", comment.id);
			});
		}
		cm.setGutterMarker(parseInt(line), "CodeMirror-comments", $gutter[0]);		
	},
	openSelectionPopover: function(mark, markUrl, loggedIn) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;	
		var ch = (mark.beginChar + mark.endChar)/2;
		var position = cm.charCoords({line:mark.beginLine, ch:ch});
		
		var $content = $("<div><a class='permanent'><i class='fa fa-link'></i> Permanent link of this selection</a>");
		$content.children("a.permanent").attr("href", markUrl);
		if (loggedIn) {
			$content.append("<a class='comment'><i class='fa fa-comment'></i> Add comment on this selection</a>");
			$content.children("a.comment").click(function() {
				if ($(".source-view").find("form.dirty").length != 0 
						&& !confirm("There are unsaved changes, discard and continue?")) {
					return;
				}
				var callback = $(".source-view").data("callback");
				callback("addComment", mark.beginLine, mark.beginChar, mark.endLine, mark.endChar);
			});
		} else {
			$content.append("<span class='comment'><i class='fa fa-warning'></i> Log in to comment on selection</span>");
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
		gitplex.server.sourceView.addCommentGutter(line, comments);
		gitplex.server.sourceView.highlightCommentTrigger();				
		gitplex.server.sourceView.onLayoutChange();
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
			gitplex.server.sourceView.addCommentGutter(line, comments);
		}
		gitplex.server.sourceView.highlightCommentTrigger();				
		gitplex.server.sourceView.onLayoutChange();
		
		gitplex.server.sourceView.mark(undefined, false);
	},
	onLayoutChange: function() {
		$sourceView = $('.source-view');
		$sourceView.triggerHandler('autofit', [$sourceView.outerWidth(), $sourceView.outerHeight()]);
	},
	onAddComment: function(mark) {
		gitplex.server.sourceView.exitFullScreen();
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		$sourceView.data("mark", mark);
		$(".source-view>.code").selectionPopover("close");

		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		gitplex.server.codemirror.clearSelection(cm);
		gitplex.server.sourceView.onLayoutChange();

		// mark and scroll after changing layout to make sure scroll 
		// works correctly
		gitplex.server.codemirror.mark(cm, mark, true);
		
		gitplex.server.sourceView.highlightCommentTrigger();
	},
	onOpenComment: function(comment) {
		gitplex.server.sourceView.exitFullScreen();
		var $sourceView = $(".source-view");
		$sourceView.data("openComment", comment);
		$sourceView.data("mark", comment.mark);
		gitplex.server.sourceView.highlightCommentTrigger();
		gitplex.server.sourceView.onLayoutChange();
		
		// mark and scroll after changing layout to make sure scroll 
		// works correctly
		gitplex.server.sourceView.mark(comment.mark, true);
	},
	onCloseComment: function() {
		var $sourceView = $(".source-view");
		$sourceView.removeData("openComment");
		gitplex.server.sourceView.highlightCommentTrigger();
		gitplex.server.sourceView.onLayoutChange();
		gitplex.server.sourceView.mark(undefined, false);
	},
	onToggleOutline: function() {
		gitplex.server.sourceView.exitFullScreen();
		gitplex.server.sourceView.onLayoutChange();
		$(".outline-toggle").toggleClass("active");
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
	mark: function(mark, scroll) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		if (mark) {
			$(".source-view").data("mark", mark);
			gitplex.server.codemirror.mark(cm, mark, scroll);
		} else {
			$(".source-view").removeData("mark");
			gitplex.server.codemirror.clearMark(cm);			
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
            				.attr("onclick", "gitplex.server.viewState.getFromViewAndSetToHistory();");
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
            		
            		for (var line = range.from+1; line<range.to; line++) {
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
	scrollToCommentBottom: function() {
		var $body = $(".source-view>.comment>.content>.body");
		$body.scrollTop($body[0].scrollHeight);
	},
	onLineWrapModeChange: function(lineWrapMode) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		cm.setOption("lineWrapping", lineWrapMode == "Soft wrap");
	},
	onTabSizeChange: function(tabSize) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		cm.setOption("tabSize", tabSize);
	}
};

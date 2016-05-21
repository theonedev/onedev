gitplex.sourceview = {
	init: function(fileContent, filePath, mark, symbolTooltipId, 
			revision, blameInfos, commentInfos, sourceCallback, viewState, loggedIn) {
		var cm;
		
		var $sourceView = $(".source-view");
		$sourceView.data("sourceCallback", sourceCallback);
		
		var $code = $sourceView.children(".code");
		$sourceView.closest(".content").css("overflow", "hidden");
		
		function alignCommentPopovers() {
			$(".comment-popover:visible").each(function() {
				var $popover = $(this);
				var lineInfo = cm.lineInfo($popover.data("line"));
				if (lineInfo.gutterMarkers) {
					var gutter = lineInfo.gutterMarkers["CodeMirror-comments"];
					if (gutter) {
						var $commentLink = $(gutter).children("a");
						$popover.css({
							"left": $commentLink.offset().left + $commentLink.outerWidth() - $sourceView.offset().left,
							"top": $commentLink.offset().top + ($commentLink.outerHeight() - $popover.outerHeight())/2 - $sourceView.offset().top
						});
					}
				}
			});
		}
		
		$sourceView.on("autofit", function(event, width, height) {
			event.stopPropagation();
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
			
			/*
			 * initialize codemirror here when we know the container width and height
			 * as otherwise the annotatescrollbar addon is inaccurate when window 
			 * initially loads
			 */ 
			var initState = !cm;
			if (!cm) {
				var options = {
					value: fileContent, 
					readOnly: pmease.commons.isDevice()?"nocursor":true,
					theme: "eclipse",
					lineNumbers: true,
					lineWrapping: true,
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
							if (cm.getOption("fullScreen"))
								cm.setOption("fullScreen", false);
				        }
					}
				};

				cm = CodeMirror($code[0], options);
				pmease.commons.codemirror.setMode(cm, filePath);

		    	// add gutters with a timer to avoid the issue that occasionally 
		    	// gutter becomes much wider than expected
			    setTimeout(function() {
					var gutters = cm.getOption("gutters").slice();
					gutters.splice(0, 0, "CodeMirror-comments");
					cm.setOption("gutters", gutters);
					for (var line in commentInfos) {
					    if (commentInfos.hasOwnProperty(line)) {
					    	gitplex.sourceview.addCommentGutter(line, commentInfos[line][1]);
					    }
					}

					if (blameInfos) {
				    	gitplex.sourceview.blame(blameInfos);
			    	}
					gitplex.sourceview.highlightCommentTrigger();				
			    }, 10);
			    
			    function onSelectText() {
			    	if (cm.hasFocus()) {
				    	var from = cm.getCursor("from");
				    	var to = cm.getCursor("to");
				    	if (from.line != to.line || from.ch != to.ch) {
				    		sourceCallback("openSelectionPopup", from.line, from.ch, to.line, to.ch);
				    	} else {
				    		$("#selection-popup").hide();
				    	}
			    	}
			    }

			    $code.on("mouseup", function() {
			    	onSelectText();
			    });
			    $code.on("keyup", function(e) {
			    	if (e.which == 37 || e.which == 38 || e.which == 39 || e.which == 40) {
			    		onSelectText();
			    	}
			    });

			    $code.mouseover(function(e) {
					var node = e.target || e.srcElement, $node = $(node);
					if ($node.hasClass("cm-property") || $node.hasClass("cm-variable") || $node.hasClass("cm-variable-2") 
							|| $node.hasClass("cm-variable-3") || $node.hasClass("cm-def") || $node.hasClass("cm-meta")) {
						document.getElementById(symbolTooltipId).onMouseOverSymbol(revision, node);
					}
			    });
				cm.setSize($code.width(), $code.height());
			    if (mark)
			    	pmease.commons.codemirror.mark(cm, mark, true);
				if (initState)
					pmease.commons.codemirror.initState(cm, viewState);
				cm.on("scroll", function() {
					gitplex.mouseState.moved = false;					
					alignCommentPopovers(cm);					
				});
			} else {
				cm.setSize($code.width(), $code.height());
				if (cm.getOption("fullScreen"))
					cm.setOption("fullScreen", false);
				alignCommentPopovers(cm);
			}
		});
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
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var uri = new URI(window.location.href); 
		var markStr = uri.search(true).mark;
		if (markStr) {
			var splitted = markStr.split("-");
			var fromInfo = splitted[0].split(".");
			var toInfo = splitted[1].split(".");
			var mark = {
				beginLine: parseInt(fromInfo[0])-1,
				beginChar: parseInt(fromInfo[1]),
				endLine: parseInt(toInfo[0])-1,
				endChar: parseInt(toInfo[1])
			}
			pmease.commons.codemirror.mark(cm, mark, false);
		} else {
			pmease.commons.codemirror.clearMark(cm);
		}
	},
	addCommentGutter: function(line, commentInfos) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var sourceCallback = $(".source-view").data("sourceCallback");
		
		var $gutter = $(document.createElement("div"));
		$gutter.addClass("CodeMirror-comment");
		$gutter.data("commentInfos", commentInfos);
		if (commentInfos.length != 1) {
			$gutter.append("<a><i class='fa fa-comments'></i></a>");
			var $commentLink = $gutter.children("a");
			var content = "";
			for (var i in commentInfos) {
				var commentInfo = commentInfos[i];
				var index = parseInt(i) + 1;
				content += "<a class='comment-trigger' title='Click to show comment of marked text'>#" + index + "</a>";
			}
			$commentLink.popover({
				html: true, 
				container: ".source-view",
				placement: "right auto",
				template: "<div data-line='" + line + "' class='popover comment-popover'><div class='arrow'></div><div class='popover-content'></div></div>",
				content: content
			});
			$commentLink.on('shown.bs.popover', function () {
				$(".comment-popover[data-line='" + line + "'] a").each(function() {
					$(this).mouseover(function() {
						var commentInfo = commentInfos[$(this).index()];			        						
						pmease.commons.codemirror.mark(cm, commentInfo.mark, false);
					});
					$(this).mouseout(function() {
						gitplex.sourceview.restoreMark();
					});
					$(this).click(function() {
    					if ($(".source-view form.dirty").length != 0 
    							&& !confirm("There are unsaved changes, discard and continue?")) {
    						return;
    					}
						var commentInfo = commentInfos[$(this).index()];			        						
						sourceCallback("openComment", commentInfo.id);
					});
				});
				gitplex.sourceview.highlightCommentTrigger();				
			});
		} else {
			var commentInfo = commentInfos[0];
			$gutter.append("<a class='comment-trigger' title='Click to show comment of marked text'><i class='fa fa-commenting'></i></a>");
			var $commentLink = $gutter.children("a");
			$commentLink.mouseover(function() {
				pmease.commons.codemirror.mark(cm, commentInfo.mark, false);
			});
			$commentLink.mouseout(function() {
				gitplex.sourceview.restoreMark();
			});
			$commentLink.click(function() {
				if ($(".source-view form.dirty").length != 0 
						&& !confirm("There are unsaved changes, discard and continue?")) {
					return;
				}
				sourceCallback("openComment", commentInfo.id);
			});
		}
		cm.setGutterMarker(parseInt(line), "CodeMirror-comments", $gutter[0]);		
	},
	openSelectionPopup: function(mark, markUrl, loggedIn) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;	
		var $sourceView = $(".source-view");
		var sourceCallback = $sourceView.data("sourceCallback");
		var ch = (mark.beginChar + mark.endChar)/2;
		var position = cm.charCoords({line:mark.beginLine, ch:ch});
		var permanentLinkCallback = function($permanentLink) {
			$permanentLink.attr("href", markUrl);
		};
		var commentLinkCallback = function($commentLink) {
			$commentLink.off("click");
			if (loggedIn) {
				$commentLink.click(function() {
					if ($sourceView.find("form.dirty").length != 0 
							&& !confirm("There are unsaved changes, discard and continue?")) {
						return;
					}
    				$("#selection-popup").hide();
					pmease.commons.codemirror.clearSelection(cm);
					pmease.commons.codemirror.mark(cm, mark, false);
    				sourceCallback("addComment", mark.beginLine, mark.beginChar, mark.endLine, mark.endChar);
				});
			} else {
				$commentLink.html("Log in to comment on selection");
			}
		};
		$("#selection-popup").data("open")(
				position, permanentLinkCallback, commentLinkCallback, $(".source-view>.code")[0]);
	},
	onCommentAdded: function(line, commentInfo) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var sourceCallback = $(".source-view").data("sourceCallback");		
		var lineInfo = cm.lineInfo(line);
		var gutter;
		if (lineInfo.gutterMarkers)
			gutter = lineInfo.gutterMarkers["CodeMirror-comments"];
		var commentInfos;
		if (gutter) {
			commentInfos = $(gutter).data("commentInfos");
		} else {
			commentInfos = [];
		} 
		commentInfos.push(commentInfo);
		gitplex.sourceview.addCommentGutter(line, commentInfos);
		gitplex.sourceview.highlightCommentTrigger();				
	},
	onCommentDeleted: function(line, commentId) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		var sourceCallback = $(".source-view").data("sourceCallback");		
		var lineInfo = cm.lineInfo(line);
		var $gutter = $(lineInfo.gutterMarkers["CodeMirror-comments"]);
		var commentInfos = $gutter.data("commentInfos");
		if (commentInfos.length == 1) {
			cm.setGutterMarker(line, "CodeMirror-comments", null);
		} else {
			for (var i in commentInfos) {
				var commentInfo = commentInfos[i];
				if (commentInfo.id == commentId) {
					commentInfos.splice(i, 1);
					break;
				}
			}
			gitplex.sourceview.addCommentGutter(line, commentInfos);
			$(".comment-popover[data-line='" + line + "']").remove();
		}
		gitplex.sourceview.highlightCommentTrigger();				
	},
	onLayoutChange: function() {
		$sourceView = $('.source-view');
		$sourceView.trigger('autofit', [$sourceView.outerWidth(), $sourceView.outerHeight()]);
	},
	onAddingComment: function() {
		gitplex.sourceview.highlightCommentTrigger();
		gitplex.sourceview.onLayoutChange();
	},
	onOpenComment: function(commentInfo) {
		gitplex.sourceview.highlightCommentTrigger();
		gitplex.sourceview.onLayoutChange();
		if (commentInfo)
			gitplex.sourceview.mark(commentInfo.mark, false);
		else
			gitplex.sourceview.restoreMark();
	},
	onToggleOutline: function() {
		gitplex.sourceview.onLayoutChange();
	},
	highlightCommentTrigger: function() {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;
		$(".comment-popover a").removeClass("active");
		for (var i=0; i<cm.lineCount(); i++) {
			var gutterMarkers = cm.lineInfo(i).gutterMarkers;
			if (gutterMarkers) {
				var gutter = gutterMarkers["CodeMirror-comments"];
				if (gutter) {
					$(gutter).children("a").removeClass("active");
				}
			}
		}
		var $comment = $(".source-view>.comment>.content>.body");
		if ($comment.length != 0) {
			var line = parseInt($comment.data("line"));
			var lineInfo = cm.lineInfo(line);
			if (lineInfo && lineInfo.gutterMarkers) {
				var gutter = lineInfo.gutterMarkers["CodeMirror-comments"];
				if (gutter) {
					var commentInfos = $(gutter).data("commentInfos");
					if (commentInfos.length == 1) {
						$(gutter).children("a").addClass("active");
					} else {
						var commentId = $comment.data("comment");
						$(".comment-popover[data-line='" + line + "'] a").each(function() {
							var commentInfo = commentInfos[$(this).index()];			        						
							if (commentInfo.id == commentId) {
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
		pmease.commons.codemirror.mark(cm, mark, scroll);
	},
	blame: function(blameInfos) {
		var cm = $(".source-view>.code>.CodeMirror")[0].CodeMirror;		
		
		if (blameInfos) {
			var gutters = cm.getOption("gutters").slice();
			gutters.splice(1, 0, "CodeMirror-annotations");
			cm.setOption("gutters", gutters);
    		for (var i in blameInfos) {
    			var blameInfo = blameInfos[i];
        		for (var j in blameInfo.ranges) {
        			var range = blameInfo.ranges[j];
        			var $gutter = $(document.createElement("div"));
        			$gutter.addClass("CodeMirror-annotation");
            		$("<a class='hash'>" + blameInfo.hash + "</a>").appendTo($gutter).attr("href", blameInfo.url).attr("title", blameInfo.message);
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
	onBlame: function(blameInfos) {
		gitplex.sourceview.blame(blameInfos);
		gitplex.sourceview.onLayoutChange();
	}
}

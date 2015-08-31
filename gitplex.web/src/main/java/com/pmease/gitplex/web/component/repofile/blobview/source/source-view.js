gitplex.sourceview = {
	init: function(codeId, fileContent, filePath, tokenPos, ajaxIndicatorUrl, symbolQuery, 
			blameCommits, commentId, addCommentCallback) {
		var cm;
		
		var $code = $("#" + codeId);
		var $sourceView = $code.closest(".source-view");
		$sourceView.closest(".body").css("overflow", "hidden");
		
		var $outlineToggle = $(".outline-toggle");
		var $outline = $sourceView.find(">.outline");
		var cookieKey = "sourceView.outline";
		if ($outlineToggle.length != 0) {
			if (Cookies.get(cookieKey) === "no") {
				$outline.hide();
				$outlineToggle.removeClass("active");
			}
			$outlineToggle.click(function() {
				$outline.toggle();
				$outlineToggle.toggleClass("active");
				if ($outline.is(":visible")) 
					Cookies.set(cookieKey, "yes", {expires: Infinity});
				else 
					Cookies.set(cookieKey, "no", {expires: Infinity});
				$sourceView.trigger("autofit", [$sourceView.outerWidth(), $sourceView.outerHeight()]);
			});
		}
		
		gitplex.expandable.getScrollTop = function() {
			if (cm)
				return cm.getScrollInfo().top;
			else
				return 0;
		};
		
		gitplex.expandable.setScrollTop = function(scrollTop) {
			if (cm)
				cm.scrollTo(undefined, scrollTop);
		};
		
		$sourceView.on("autofit", function(event, width, height) {
			event.stopPropagation();
			$sourceView.outerWidth(width);
			$sourceView.outerHeight(height);
			$code.outerHeight($sourceView.height());
			var $outline = $sourceView.find(">.outline");
			if ($outline.is(":visible")) {
				$code.outerWidth($sourceView.width()/4.0*3);
				$outline.outerWidth($sourceView.width() - $code.outerWidth()-1);
				$outline.outerHeight($sourceView.height());
			} else {
				$code.outerWidth($sourceView.width());
			}
			
			/*
			 * initialize codemirror here when we know the container width and height
			 * as otherwise the annotatescrollbar addon is inaccurate when window 
			 * initially loads
			 */ 
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
					tokenHover: {
						getTooltip: function(tokenEl) {
							var tooltip = document.createElement("div");
							var $tooltip = $(tooltip);
							$tooltip.html("<img src=" + ajaxIndicatorUrl + "></img>");
							$tooltip.attr("id", codeId + "-symbolstooltip");
							symbolQuery($(tokenEl).text());
							return tooltip;
						} 
					},
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
				
				if (addCommentCallback) {
					$code[0].addCommentCallback = addCommentCallback;
					var gutter = "CodeMirror-addcomments";
					var gutters = cm.getOption("gutters").slice();
					gutters.push(gutter);
					cm.setOption("gutters", gutters);
					for (var line=0; line<cm.lineCount(); line++) {
		    			var $ele = $(document.createElement("div"));
		    			$ele.addClass("CodeMirror-addcomment");
		    			$ele.attr("title", "Add inline comment");
		    			var script = "document.getElementById(\"" + codeId + "\").addCommentCallback(" + line + ");";
		        		$("<a href='javascript: " + script + "'><i class='fa fa-plus-square-o'></i></a>").appendTo($ele);
						cm.setGutterMarker(line, gutter, $ele[0]);
					}
				}
				
			    var modeInfo = CodeMirror.findModeByFileName(filePath);
			    if (modeInfo) {
			    	// specify mode via mime does not work for gfm (github flavored markdown)
			    	if (modeInfo.mode === "gfm")
			    		cm.setOption("mode", "gfm");
			    	else
			    		cm.setOption("mode", modeInfo.mime);
					CodeMirror.autoLoadMode(cm, modeInfo.mode);
			    }

			    if (tokenPos)
			    	gitplex.sourceview.highlightToken(cm, tokenPos);

			    cm.on("scroll", function() {
			    	var scrollInfo = cm.getScrollInfo();
			    	pmease.commons.history.setScrollPos({left: scrollInfo.left, top: scrollInfo.top});
			    });
			    var scrollPos = pmease.commons.history.getScrollPos();
			    if (scrollPos)
			    	cm.scrollTo(scrollPos.left, scrollPos.top);
			    
			    var cursorTimer;
			    cm.on("cursorActivity", function() {
		    		if (cursorTimer)
		    			clearTimeout(cursorTimer);
			    	cursorTimer = setTimeout(function() {
			    		cursorTimer = undefined;
				    	pmease.commons.history.setCursor(cm.getCursor());
			    	}, 1000);
			    });
			    
			    var cursor = pmease.commons.history.getCursor();
			    if (cursor)
			    	cm.setCursor(cursor);
			    
			    if (blameCommits) {
			    	// render blame blocks with a timer to avoid the issue that occasionally 
			    	// blame gutter becomes much wider than expected
			    	setTimeout(function() {
				    	gitplex.sourceview.blame(cm, blameCommits);
			    	}, 10);
			    }

			    $sourceView.find(">.comment").each(function() {
					var lineNo = $(this).data("lineno");
					gitplex.sourceview.placeComment(cm, lineNo, this);
				});
			    if (commentId != -1) {
			    	$comment = $("#pullrequest-comment-" + commentId);
			    	cm.setCursor($comment.data("lineno"));
			    	setTimeout(function() {$comment.focus();}, 10);
			    }
			    
			    cm.focus();
			} 
			if (cm.getOption("fullScreen"))
				cm.setOption("fullScreen", false);
			cm.setSize($code.width(), $code.height());
		});
	}, 
		
	symbolsQueried: function(codeId, symbolsId) {
		var $symbols = $("#" + symbolsId);
		var $tooltip = $("#" + codeId + "-symbolstooltip");
		$tooltip.children().remove();
		$symbols.children().appendTo($tooltip);
		$tooltip.align();
	},
	
	highlightToken: function(cm, tokenPos) {
		if (typeof cm === "string") 
			cm = $("#"+ cm + ">.CodeMirror")[0].CodeMirror;		
		
		if (tokenPos) {
			var h = cm.getScrollInfo().clientHeight;
			var coords = cm.charCoords({line: tokenPos.line, ch: 0}, "local");
			cm.scrollTo(null, (coords.top + coords.bottom - h) / 2); 			
			
			if (tokenPos.range) {
				var anchor = {line: tokenPos.line, ch: tokenPos.range.start};
				var head = {line: tokenPos.line, ch: tokenPos.range.end}; 
				cm.setSelection(anchor, head);
			} else {
				cm.setCursor(tokenPos.line);
			}
		} else {
			cm.setCursor(0, 0);
		}
	},
	
	placeComment: function(cm, lineNo, comment) {
		if (typeof cm === "string") 
			cm = $("#"+ cm + ">.CodeMirror")[0].CodeMirror;		
		if (typeof comment === "string")
			comment = $("<div class='comment'><div id='" + comment + "'></div></div>")[0];
		comment.lineWidget = cm.addLineWidget(lineNo, comment, {coverGutter: true});
	},
	
	commentResized: function($comment) {
		if (typeof $comment === "string") 
			$comment = $("#" + $comment);
		$comment[0].lineWidget.changed();
		var $wrapper = $comment.find(">div");
		$wrapper.on("mouseup resized", function() {
			$comment[0].lineWidget.changed();
		});
		$wrapper.on("fullscreen", function() {
			// full screen mode is abnormal if we do not do this
			$("body").append($wrapper);
		});
		$wrapper.on("exitFullscreen", function() {
			$comment.append($wrapper);
			$comment[0].lineWidget.changed();
		});
	},
	
	blame: function(cm, blameCommits) {
		if (typeof cm === "string") 
			cm = $("#"+ cm + ">.CodeMirror")[0].CodeMirror;		
		
		if (blameCommits) {
			var gutters = cm.getOption("gutters").slice();
			gutters.splice(0, 0, "CodeMirror-annotations");
			cm.setOption("gutters", gutters);
    		for (var i in blameCommits) {
    			var commit = blameCommits[i];
        		for (var j in commit.ranges) {
        			var range = commit.ranges[j];
        			var $ele = $(document.createElement("div"));
        			$ele.addClass("CodeMirror-annotation");
            		$("<a class='hash'>" + commit.hash + "</a>").appendTo($ele).attr("href", commit.url).attr("title", commit.message);
            		$ele.append("<span class='date'>" + commit.authorDate + "</span>");
            		$ele.append("<span class='author'>" + commit.authorName + "</span>");
            		cm.setGutterMarker(range.beginLine, "CodeMirror-annotations", $ele[0]);
            		
            		for (var line = range.beginLine+1; line<range.endLine; line++) {
            			var $ele = $(document.createElement("div"));
            			$ele.addClass("CodeMirror-annotation");
                		$ele.append("<span class='same-as-above fa fa-arrow-up' title='same as above'></span>");
                		cm.setGutterMarker(line, "CodeMirror-annotations", $ele[0]);
            		}
        		}
    		}    		
		} else {
			cm.clearGutter("CodeMirror-annotations");
			var gutters = cm.getOption("gutters").slice();
			gutters.splice(0, 1);
			cm.setOption("gutters", gutters);
		}
	}
	
}

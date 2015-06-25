gitplex.sourceview = {
	init: function(codeId, fileContent, filePath, tokenPos, ajaxIndicatorUrl, symbolQuery, blames) {
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
		
		gitplex.spaceGreedy.getScrollTop = function() {
			if (cm)
				return cm.getScrollInfo().top;
			else
				return 0;
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
					readOnly: true,
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
					gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
				};
				cm = CodeMirror($code[0], options);
				
			    var modeInfo = CodeMirror.findModeByFileName(filePath);
			    if (modeInfo) {
			    	// specify mode via mime does not work for gfm (github flavored markdown)
			    	if (modeInfo.mode === "gfm")
			    		cm.setOption("mode", "gfm");
			    	else
			    		cm.setOption("mode", modeInfo.mime);
					CodeMirror.autoLoadMode(cm, modeInfo.mode);
			    }

			    cm.on("scroll", function() {
			    	var scrollInfo = cm.getScrollInfo();
			    	pmease.commons.history.setScrollPos({left: scrollInfo.left, top: scrollInfo.top});
			    });
			    
			    if (tokenPos)
			    	gitplex.sourceview.highlightToken(cm, tokenPos);

			    var scrollPos = pmease.commons.history.getScrollPos();
			    if (scrollPos)
			    	cm.scrollTo(scrollPos.left, scrollPos.top);
			    
			    if (blames) {
			    	// render blame blocks with a timer to avoid the issue that occasionally 
			    	// blame gutter becomes much wider than expected
			    	setTimeout(function() {
				    	gitplex.sourceview.blame(cm, blames);
			    	}, 10);
			    }
			} 
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
		
	},
	
	blame: function(cm, blames) {
		if (typeof cm === "string") 
			cm = $("#"+ cm + ">.CodeMirror")[0].CodeMirror;		
		
		if (blames) {
    		cm.setOption("gutters", ["CodeMirror-annotations", "CodeMirror-linenumbers", "CodeMirror-foldgutter"]);
    		for (var i in blames) {
    			var blame = blames[i];
        		for (var j in blame.ranges) {
        			var $ele = $(document.createElement("div"));
        			$ele.addClass("CodeMirror-annotation");
            		$("<a class='hash'>" + blame.hash + "</a>").appendTo($ele).attr("href", blame.url).attr("title", blame.message);
            		$ele.append("<span class='date'>" + blame.authorDate + "</span>");
            		$ele.append("<span class='author'>" + blame.authorName + "</span>");
            		cm.setGutterMarker(blame.ranges[j].beginLine, "CodeMirror-annotations", $ele[0]);
        		}
    		}    		
		} else {
			cm.clearGutter("CodeMirror-annotations");
			cm.setOption("gutters", ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]);
		}
	}
	
}

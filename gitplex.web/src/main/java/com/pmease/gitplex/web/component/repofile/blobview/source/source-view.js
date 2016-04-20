gitplex.sourceview = {
	init: function(codeId, fileContent, filePath, mark, symbolTooltipId, revision, blameCommits, viewState) {
		var cm;
		
		var $code = $("#" + codeId);
		var $sourceView = $code.closest(".source-view");
		$sourceView.closest(".content").css("overflow", "hidden");
		
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

			    if (mark)
			    	pmease.commons.codemirror.mark(cm, mark);

			    if (blameCommits) {
			    	// render blame blocks with a timer to avoid the issue that occasionally 
			    	// blame gutter becomes much wider than expected
			    	setTimeout(function() {
				    	gitplex.sourceview.blame(cm, blameCommits);
			    	}, 10);
			    }
			    
			    $code.on("mouseup keyup", function() {
			    	var from = cm.getCursor("from");
			    	var to = cm.getCursor("to");
			    	if (from.line != to.line || from.ch != to.ch) {
		    			var ch = (from.ch + to.ch)/2;
		    			var position = cm.charCoords({line:from.line, ch:ch});
		    			var uri = URI(window.location.href); 
		    			uri.removeSearch("mark").addSearch("mark", 
		    					(from.line+1) + "," + (from.ch+1) + "-" + (to.line+1) + "," + (to.ch+1));
			    		$("#selection-popup").data("show")(position, uri.toString(), function(){});
			    	} else {
			    		$("#selection-popup").hide();
			    	}
			    });

			    $code.mouseover(function(e) {
					var node = e.target || e.srcElement, $node = $(node);
					if ($node.hasClass("cm-property") || $node.hasClass("cm-variable") || $node.hasClass("cm-variable-2") 
							|| $node.hasClass("cm-variable-3") || $node.hasClass("cm-def") || $node.hasClass("cm-meta")) {
						document.getElementById(symbolTooltipId).onMouseOverSymbol(revision, node);
					}
			    });
			} 
			if (cm.getOption("fullScreen"))
				cm.setOption("fullScreen", false);
			cm.setSize($code.width(), $code.height());
			if (initState)
				pmease.commons.codemirror.initState(cm, viewState);
		});
	}, 
	mark: function(codeId, mark) {
		var cm = $("#"+ codeId + ">.CodeMirror")[0].CodeMirror;		
		pmease.commons.codemirror.mark(cm, mark);
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
            		$ele.append("<span class='date'>" + commit.commitDate + "</span>");
            		$ele.append("<span class='author'>" + commit.authorName + "</span>");
            		cm.setGutterMarker(range.from, "CodeMirror-annotations", $ele[0]);
            		
            		for (var line = range.from+1; line<range.to; line++) {
            			var $ele = $(document.createElement("div"));
            			$ele.addClass("CodeMirror-annotation");
                		$ele.append("<span class='same-as-above'>...</span>");
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

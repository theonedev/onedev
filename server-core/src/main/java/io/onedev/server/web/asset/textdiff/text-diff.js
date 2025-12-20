onedev.server.textDiff = {
	/**
	 * Save syntax highlighting class names for text at specific positions
	 */
	saveHighlightedSyntaxes: function(syntaxes, text, style, lineIndex, beginPos) {
		if (lineIndex != undefined && beginPos != undefined && style != null) {
			var classNames = "cm-" + style.replace(/ +/g, " cm-");
			var lineSyntaxes = syntaxes[lineIndex];
			if (lineSyntaxes == undefined) {
				lineSyntaxes = [];
				syntaxes[lineIndex] = lineSyntaxes;
			}
			for (var i=0; i<text.length; i++)
				lineSyntaxes[i+beginPos] = classNames;
		}
	},
	
	/**
	 * Extract line content from diff rows for syntax highlighting
	 * Supports both unified diff format and split (side-by-side) format
	 */
	extractLinesForSyntaxHighlight: function($rows, isSplitView) {
		var oldLines = [], newLines = [];
		
		$rows.each(function() {
			var $row = $(this);
			var $content = $row.children(".content");
			
			if (isSplitView) {
				// Split view: two content columns per row
				if ($content.length == 2) {
					var $old = $content.first();
					if (!$old.hasClass("none"))
						oldLines.push($old.text());
					
					var $new = $content.last();
					if (!$new.hasClass("none"))
						newLines.push($new.text());
				}
			} else {
				// Unified view: one content column per row
				if ($content.hasClass("old") && $content.hasClass("new")) {
					// Modified line with inline changes
					var oldPartials = [];
					var newPartials = [];
					
					$content.contents().each(function() {
						var $this = $(this);
						var text = $this.text();
						if ($this.hasClass("delete")) {
							oldPartials.push(text);
						} else if ($this.hasClass("insert")) {
							newPartials.push(text);			
						} else {
							oldPartials.push(text);
							newPartials.push(text);					
						}
					});
					oldLines.push(oldPartials.join(""));
					newLines.push(newPartials.join(""));
				} else if ($content.hasClass("old")) {
					oldLines.push($content.text());
					newLines.push("");
				} else if ($content.hasClass("new")) {
					oldLines.push("");
					newLines.push($content.text());
				} else {
					// Equal line
					oldLines.push($content.text());
					newLines.push($content.text());
				}
			}
		});
		
		return {oldLines: oldLines, newLines: newLines};
	},
	
	/**
	 * Apply syntax highlighting to diff content
	 * Supports both unified and split view formats
	 */
	applySyntaxHighlightToContent: function($td, oldLineSyntaxes, newLineSyntaxes, isSplitView) {
		var $highlighted = $("<div></div>");
		
		var partial = [];
		var classNames = "";
		function appendPartial() {
			if (partial.length != 0) {
				var $span = $("<span></span>");
				$span.text(partial.join(""));
				$span.attr("class", classNames);
				$highlighted.append($span);
			}
		}
		
		var oldPos = 0, newPos = 0;
		$td.contents().each(function() {
			var $content = $(this);
			var text = $content.text();
			var lineSyntaxes;
			var pos;
			
			// Determine which syntax array to use based on content type
			if ($td.hasClass("old") && $td.hasClass("new")) {
				// Unified view: modified line with inline changes
				if ($content.hasClass("insert")) { 
					lineSyntaxes = newLineSyntaxes;
					pos = newPos;
					newPos += text.length;
				} else if ($content.hasClass("delete")) {
					lineSyntaxes = oldLineSyntaxes;
					pos = oldPos;
					oldPos += text.length;
				} else { 
					lineSyntaxes = oldLineSyntaxes;
					pos = oldPos;
					oldPos += text.length;
					newPos += text.length;
				}
			} else if ($td.hasClass("new")) {
				lineSyntaxes = newLineSyntaxes;
				pos = newPos;
				newPos += text.length;
			} else if ($td.hasClass("old")) {
				lineSyntaxes = oldLineSyntaxes;
				pos = oldPos;
				oldPos += text.length;
			} else if ($td.hasClass("right")) {
				// Split view: right side (new)
				lineSyntaxes = newLineSyntaxes;
				pos = newPos;
				oldPos += text.length;
				newPos += text.length;
			} else {
				// Split view: left side (old) or unified view: equal
				lineSyntaxes = oldLineSyntaxes;
				pos = oldPos;
				oldPos += text.length;
				newPos += text.length;
			}
			
			// Read base class names once per content node
			var baseClassNames = $content[0].className;
			if (baseClassNames == undefined)
				baseClassNames = "";
			
			// Apply syntax highlighting to each character
			for (var i=0; i<text.length; i++) {
				var currentClassNames = baseClassNames;
				
				if (lineSyntaxes) {
					var syntaxClassNames = lineSyntaxes[pos+i];
					if (syntaxClassNames != undefined && syntaxClassNames.length != 0) {
						if (currentClassNames.length != 0)
							currentClassNames += " ";
						currentClassNames += syntaxClassNames;	
					}
				}
				if (currentClassNames != classNames) {
					appendPartial();
					partial = [];
					classNames = currentClassNames;
				}
				partial.push(text.charAt(i));
			}
		});
		appendPartial();
		
		$td.html($highlighted.html());
	},
	
	/**
	 * Highlight syntax for unified diff view
	 * Used by TextDiffPanel and BlobTextDiffPanel (unified mode)
	 */
	highlightSyntaxForUnifiedDiff: function($rows, oldFileName, newFileName, onComplete) {
		var lines = onedev.server.textDiff.extractLinesForSyntaxHighlight($rows, false);
		var oldSyntaxes = [];
		var oldProcessed = false;
		var newSyntaxes = [];
		var newProcessed = false;

		function doneHighlight() {
			if (oldProcessed && newProcessed) {
				var oldLineIndex = 0;
				var newLineIndex = 0;
				$rows.each(function() {
					var $row = $(this);					
					var $td = $row.children(".content");
					
					var oldLineSyntaxes = oldSyntaxes[oldLineIndex];
					var newLineSyntaxes = newSyntaxes[newLineIndex];
					
					onedev.server.textDiff.applySyntaxHighlightToContent($td, oldLineSyntaxes, newLineSyntaxes, false);
					
					oldLineIndex++;
					newLineIndex++;
				});
				
				if (onComplete)
					onComplete();
			}
		}	
				
		var oldModeInfo = onedev.server.codemirror.findModeByFileName(oldFileName);
		if (oldModeInfo) {
			onedev.server.codemirror.highlightSyntax(
				lines.oldLines.join("\n"), 
				oldModeInfo, 
				function(text, style, lineIndex, beginPos) {
					onedev.server.textDiff.saveHighlightedSyntaxes(oldSyntaxes, text, style, lineIndex, beginPos);
				}, 
				undefined,
				function() {
					oldProcessed = true;
					doneHighlight();							
				}
			);				
		} else {
			oldProcessed = true;
			doneHighlight();
		}
		
		var newModeInfo = onedev.server.codemirror.findModeByFileName(newFileName);
		if (newModeInfo) {
			onedev.server.codemirror.highlightSyntax(
				lines.newLines.join("\n"), 
				newModeInfo, 
				function(text, style, lineIndex, beginPos) {
					onedev.server.textDiff.saveHighlightedSyntaxes(newSyntaxes, text, style, lineIndex, beginPos);
				}, 
				undefined,
				function() {
					newProcessed = true;
					doneHighlight();							
				}
			);				
		} else {
			newProcessed = true;
			doneHighlight();
		}
	},
	
	/**
	 * Highlight syntax for split diff view (side-by-side)
	 * Used by BlobTextDiffPanel (split mode)
	 */
	highlightSyntaxForSplitDiff: function($rows, oldFileName, newFileName, onComplete) {
		var lines = onedev.server.textDiff.extractLinesForSyntaxHighlight($rows, true);
		var oldSyntaxes = [];
		var oldProcessed = false;
		var newSyntaxes = [];
		var newProcessed = false;

		function doneHighlight() {
			if (oldProcessed && newProcessed) {
				var oldLineIndex = 0;
				var newLineIndex = 0;
				$rows.each(function() {
					var $row = $(this);					
					$row.children("td.content:not(.none)").each(function() {
						var $td = $(this);
						
						var oldLineSyntaxes = oldSyntaxes[oldLineIndex];
						var newLineSyntaxes = newSyntaxes[newLineIndex];
						
						onedev.server.textDiff.applySyntaxHighlightToContent($td, oldLineSyntaxes, newLineSyntaxes, true);
					});
					
					var $contents = $row.children("td.content");
					if ($contents.length == 2) {
						if (!$contents.first().hasClass("none"))
							oldLineIndex++;
						if (!$contents.last().hasClass("none"))
							newLineIndex++;
					} else {
						if ($contents.hasClass("old") || $contents.hasClass("equal"))
							oldLineIndex++;
						if ($contents.hasClass("new") || $contents.hasClass("equal"))
							newLineIndex++;
					}
				});
				
				if (onComplete)
					onComplete();
			}
		}	
				
		var oldModeInfo = onedev.server.codemirror.findModeByFileName(oldFileName);
		if (oldModeInfo) {
			onedev.server.codemirror.highlightSyntax(
				lines.oldLines.join("\n"), 
				oldModeInfo, 
				function(text, style, lineIndex, beginPos) {
					onedev.server.textDiff.saveHighlightedSyntaxes(oldSyntaxes, text, style, lineIndex, beginPos);
				}, 
				undefined, 
				function() {
					oldProcessed = true;
					doneHighlight();							
				}
			);				
		} else {
			oldProcessed = true;
			doneHighlight();
		}
		
		var newModeInfo = onedev.server.codemirror.findModeByFileName(newFileName);
		if (newModeInfo) {
			onedev.server.codemirror.highlightSyntax(
				lines.newLines.join("\n"), 
				newModeInfo, 
				function(text, style, lineIndex, beginPos) {
					onedev.server.textDiff.saveHighlightedSyntaxes(newSyntaxes, text, style, lineIndex, beginPos);
				}, 
				undefined,
				function() {
					newProcessed = true;
					doneHighlight();							
				}
			);				
		} else {
			newProcessed = true;
			doneHighlight();
		}
	}
};

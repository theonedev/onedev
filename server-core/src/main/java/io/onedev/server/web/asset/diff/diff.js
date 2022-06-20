onedev.server.diff = {
	highlightSyntax: function($textDiff, fileName) {
		if ($textDiff.hasClass("syntax-highlighted"))
			return;
			
		$textDiff.addClass("cm-s-eclipse");
		
		var $trs = $textDiff.find("tr");
		
		var oldLines = [], newLines = [];
		
		$trs.each(function() {
			var $tr = $(this);
			var $content = $tr.children(".content");
			if ($content.hasClass("old") && $content.hasClass("new")) {
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
			} else if ($content.hasClass("new")) {
				newLines.push($content.text());
			} else {
				oldLines.push($content.text());
				newLines.push($content.text());
			}
		});
		
		function saveSyntaxes(syntaxes, text, style, lineIndex, beginPos) {
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
		}
		
		var oldSyntaxes = [];
		var oldProcessed = false;
		var newSyntaxes = [];
		var newProcessed = false;

		function doneHighlight() {
			if (oldProcessed && newProcessed) {
				var oldLineIndex = 0;
				var newLineIndex = 0;
				$trs.each(function() {
					var $tr = $(this);					
					var $td = $tr.children(".content");
					
					var oldLineSyntaxes = oldSyntaxes[oldLineIndex];
					var newLineSyntaxes = newSyntaxes[newLineIndex];
					
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
					
					var oldPos = newPos = 0;
					$td.contents().each(function() {
						var $content = $(this);
						var text = $content.text();
						var lineSyntaxes;
						var pos;
						if ($td.hasClass("old") && $td.hasClass("new")) {
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
						} else {
							lineSyntaxes = oldLineSyntaxes;
							pos = oldPos;
							oldPos += text.length;
							newPos += text.length;
						}
						
						for (var i=0; i<text.length; i++) {
							var currentClassNames = $content[0].className;
							if (currentClassNames == undefined)
								currentClassNames = "";
							
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
					})
					appendPartial();
					
					$td.html($highlighted.html());
					
					if ($td.hasClass("old") || $td.hasClass("equal"))
						oldLineIndex++;
					if ($td.hasClass("new") || $td.hasClass("equal"))
						newLineIndex++;
				});
				
				$textDiff.addClass("syntax-highlighted");
			}
		}	
				
		var modeInfo = onedev.server.codemirror.findModeByFileName(fileName);
		if (modeInfo) {
			onedev.server.codemirror.highlightSyntax(
				oldLines.join("\n"), 
				modeInfo, 
				function(text, style, lineIndex, beginPos) {
					saveSyntaxes(oldSyntaxes, text, style, lineIndex, beginPos);
				}, 
				undefined,
				function() {
					oldProcessed = true;
					doneHighlight();							
				}
			);				
			onedev.server.codemirror.highlightSyntax(
				newLines.join("\n"), 
				modeInfo, 
				function(text, style, lineIndex, beginPos) {
					saveSyntaxes(newSyntaxes, text, style, lineIndex, beginPos);
				}, 
				undefined,
				function() {
					newProcessed = true;
					doneHighlight();							
				}
			);				
		} else {
			oldProcessed = newProcessed = true;
			doneHighlight();
		}
	}
}
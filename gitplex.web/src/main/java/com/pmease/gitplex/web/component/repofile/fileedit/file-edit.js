gitplex.fileedit = {
	init: function(containerId, filePath, fileContent, previewCallback, saveCallback, mark, viewState) {
		var $container = $("#" + containerId);
		var $fileEdit = $container.find(">.file-edit");
		var $head = $fileEdit.find(">.head");
		var $body = $fileEdit.find(">.body");
		var $edit = $body.find(">div.edit");
		var cm;
		
	    $head.find("a.edit").click(function() {
	    	gitplex.fileedit.selectTab($(this));
	    });
	    $head.find("a.preview").click(function() {
	    	previewCallback(cm.getValue());
	    });
	    $head.find("a.save").click(function() {
	    	saveCallback(cm.getValue());
	    });
	    
		$fileEdit.on("autofit", function(event, width, height) {
			event.stopPropagation();
			$fileEdit.outerWidth(width);
			$fileEdit.outerHeight(height);
			
			height = $fileEdit.height()-$head.outerHeight();
			$body.outerWidth(width).outerHeight(height-1);

			/*
			 * initialize codemirror here when we know the container width and height
			 * as otherwise the annotatescrollbar addon is inaccurate when window 
			 * initially loads
			 */ 
			var initState = !cm;
			if (!cm) {
				cm = CodeMirror($edit[0], {
					value: fileContent, 
					theme: "eclipse",
					lineNumbers: true,
					lineWrapping: true,
					styleActiveLine: true,
					styleSelectedText: true,
					foldGutter: true,
					matchBrackets: true,
					scrollbarStyle: "simple",
					highlightIdentifiers: {delay: 500},
					extraKeys: {
						"F11": function(cm) {
							cm.setOption("fullScreen", !cm.getOption("fullScreen"));
						},
						"Esc": function(cm) {
							if (cm.getOption("fullScreen"))
								cm.setOption("fullScreen", false);
				        }
					}
				});
				
				cm.oldDocValue = cm.doc.getValue();
				cm.on("change", function() {
					var $form = $body.find(">form.edit");
					if (cm.doc.getValue() != cm.oldDocValue) {
						$form.addClass("dirty");
						$fileEdit.data("contentChanged", true);
					} else {
						$form.removeClass("dirty");
						$fileEdit.data("contentChanged", false);
					}
				});
				pmease.commons.codemirror.setMode(cm, filePath);
				
			    if (mark) 
			    	pmease.commons.codemirror.mark(cm, mark);
			}
			
			if ($edit.is(":visible")) {
				$body.css("overflow", "hidden");
				$edit.outerHeight($body.height());
				$edit.outerWidth($body.width());
				if (cm.getOption("fullScreen"))
					cm.setOption("fullScreen", false);
				cm.setSize($edit.width(), $edit.height());
				if (initState)
					pmease.commons.codemirror.initState(cm, viewState);
			} else {
				$body.css("overflow", "auto");
			}
		});
	},
	save: function(containerId) {
		gitplex.fileedit.selectTab($("#" + containerId + ">.file-edit>.head>.save"));
	},
	preview: function(containerId) {
		gitplex.fileedit.selectTab($("#" + containerId + ">.file-edit>.head>.preview"));
	},
	setMode: function(containerId, filePath) {
		var cm = $("#"+ containerId + ">.file-edit>.body>div.edit>.CodeMirror")[0].CodeMirror;		
		pmease.commons.codemirror.setMode(cm, filePath);
	},
	mark: function(containerId, mark) {
		var cm = $("#"+ containerId + ">.file-edit>.body>div.edit>.CodeMirror")[0].CodeMirror;		
		if (cm.oldDocValue) {
			var dmp = new diff_match_patch();
			var diffs = dmp.diff_main(cm.oldDocValue, cm.doc.getValue());
			
			var beginLine = mark.beginLine, beginChar = mark.beginChar;
			var endLine = mark.endLine, endChar = mark.endChar;
			var newBeginLine, newBeginChar, newEndLine, newEndChar;
			var oldLine = oldChar = newLine = newChar = 0;
			for (var i=0; i<diffs.length; i++) {
				var diff = diffs[i];
				var chars = diff[1];
				var quit = false;
				for (var j=0; j<chars.length; j++) {
					var char = chars[j];
					if (diff[0] == -1) {
						if (char == '\n') {
							oldLine++;
							oldChar = 0;
						} else {
							oldChar++;
						}
					} else if (diff[0] == 1) {
						if (char == '\n') {
							newLine++;
							newChar = 0;
						} else {
							newChar++;
						}
					} else {
						if (oldLine == beginLine && oldChar == beginChar) {
							newBeginLine = newLine;
							newBeginChar = newChar;
						} else if (oldLine == endLine && oldChar == endChar) {
							newEndLine = newLine;
							newEndChar = newChar;
						} 

						if (char == '\n') {
							oldLine++;
							oldChar = 0;
							newLine++;
							newChar = 0;
						} else {
							oldChar++;
							newChar++;
						}
					}
					if (oldLine>beginLine && !newBeginLine || oldLine>endLine) {
						quite = true;
						break;
					}
				}
			}
		}
		
		if (newBeginLine && newEndLine) {
			var newMark = {
				beginLine: newBeginLine, 
				beginChar: newBeginChar, 
				endLine: newEndLine, 
				endChar: newEndChar
			};
			pmease.commons.codemirror.mark(cm, newMark);
		}
	},
	selectTab: function($tab) {
    	var $active = $tab.parent().find("a.tab.active");
    	$active[0].hideableVisible = $(".hideable").is(":visible");
    	$active.removeClass("active");
    	$tab.addClass("active");
    	if ($tab[0].hideableVisible != undefined) {
    		if ($tab[0].hideableVisible)
    			$(".hideable").show();
    		else
    			$(".hideable").hide();
    	}
    	var $fileEdit = $tab.closest(".file-edit");
    	var $body = $fileEdit.find(">.body");
		$body.find(">div").hide();
		if ($tab.hasClass("edit")) {
			$body.find(">div.edit").show().find(">.CodeMirror")[0].CodeMirror.focus();
		} else if ($tab.hasClass("preview")) {
			$body.find(">div.preview").show();
		} else {
			$body.find(">div.save").show();
			$body.find(">div.save>.edit-save").trigger("contentEdit", [$fileEdit.data("contentChanged")]);
		}
		$(window).resize();
	}
}

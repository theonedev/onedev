gitplex.fileEdit = {
	init: function(containerId, filePath, fileContent, previewCallback, saveCallback) {
		var $container = $("#" + containerId);
		var $fileEdit = $container.find(">.file-edit");
		var $head = $fileEdit.find(">.head");
		var $body = $fileEdit.find(">.body");
		var $edit = $body.find(">div.edit");
		var cm = CodeMirror($edit[0], {
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
		
		var originalDocValue = cm.doc.getValue();
		cm.on("change", function() {
			var $form = $body.find(">form.edit");
			if (cm.doc.getValue() != originalDocValue) {
				$form.addClass("dirty");
				$fileEdit.data("contentChanged", true);
			} else {
				$form.removeClass("dirty");
				$fileEdit.data("contentChanged", false);
			}
		});
		cm.focus();
		
	    // use scroll timer and cursor timer to minimize performance impact of 
	    // remembering scroll and cursor position
	    var scrollTimer;
	    cm.on("scroll", function() {
	    	if (scrollTimer)
	    		clearTimeout(scrollTimer);
	    	scrollTimer = setTimeout(function() {
	    		scrollTimer = undefined;
		    	var scrollInfo = cm.getScrollInfo();
		    	pmease.commons.history.setScrollPos({left: scrollInfo.left, top: scrollInfo.top});
	    	}, 500);
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
	    	}, 500);
	    });
	    
	    console.log("check cursor");
	    var cursor = pmease.commons.history.getCursor();
	    if (cursor)
	    	cm.setCursor(cursor);
		
		gitplex.fileEdit.setMode(cm, filePath);
		
	    CodeMirror.keyMap.default["Ctrl-L"] = "gotoLine";
		
	    $head.find("a.edit").click(function() {
	    	gitplex.fileEdit.selectTab($(this));
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
			
			if ($edit.is(":visible")) {
				$body.css("overflow", "hidden");
				$edit.outerHeight($body.height());
				$edit.outerWidth($body.width());
				if (cm.getOption("fullScreen"))
					cm.setOption("fullScreen", false);
				cm.setSize($edit.width(), $edit.height());
			} else {
				$body.css("overflow", "auto");
			}
		});
		
		gitplex.expandable.getScrollTop = function() {
			if ($edit.is(":visible"))
				return cm.getScrollInfo().top;
			else
				return $body.scrollTop();
		};
		
		gitplex.expandable.setScrollTop = function(scrollTop) {
			if ($edit.is(":visible"))
				cm.scrollTo(undefined, scrollTop);
			else
				$body.scrollTop(scrollTop);
		};
		
	},
	save: function(containerId) {
		gitplex.fileEdit.selectTab($("#" + containerId + ">.file-edit>.head>.save"));
	},
	preview: function(containerId) {
		gitplex.fileEdit.selectTab($("#" + containerId + ">.file-edit>.head>.preview"));
	},
	setMode: function(cm, filePath) {
		if (typeof cm === "string") 
			cm = $("#"+ cm + ">.file-edit>.body>div.edit>.CodeMirror")[0].CodeMirror;		

	    var modeInfo = CodeMirror.findModeByFileName(filePath);
	    if (modeInfo) {
	    	// specify mode via mime does not work for gfm (github flavored markdown)
	    	if (modeInfo.mode === "gfm")
	    		cm.setOption("mode", "gfm");
	    	else
	    		cm.setOption("mode", modeInfo.mime);
			CodeMirror.autoLoadMode(cm, modeInfo.mode);
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

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
			highlightIdentifiers: {delay: 500}
		});
		
		cm.on("change", function() {
			$body.find(">form.edit").addClass("dirty");
		});
		cm.focus();
		
	    cm.on("scroll", function() {
	    	var scrollInfo = cm.getScrollInfo();
	    	pmease.commons.history.setScrollPos({left: scrollInfo.left, top: scrollInfo.top});
	    });
	    
	    var scrollPos = pmease.commons.history.getScrollPos();
	    if (scrollPos)
	    	cm.scrollTo(scrollPos.left, scrollPos.top);
	    
		gitplex.fileEdit.setMode(cm, filePath);
		
	    $head.find("a.edit").click(function() {
	    	$head.find("a.tab").removeClass("active");
	    	$(this).addClass("active");
			$body.find(".preview").hide();
			$body.find(".save").hide();
			$edit.show();
			$(window).resize();
	    });
	    $head.find("a.preview").click(function() {
	    	previewCallback(cm.getValue());
	    });
	    $head.find("a.save").click(function() {
	    	saveCallback(cm.getValue());
	    	$head.find("a.tab").removeClass("active");
	    	$(this).addClass("active");
	    });
	    
		$fileEdit.on("autofit", function(event, width, height) {
			console.log("autofit");
			event.stopPropagation();
			$fileEdit.outerWidth(width);
			$fileEdit.outerHeight(height);
			
			height = $fileEdit.height()-$head.outerHeight();
			$body.outerWidth(width).outerHeight(height);
			
			if ($edit.is(":visible")) {
				$body.css("overflow", "hidden");
				$edit.outerHeight($body.height());
				$edit.outerWidth($body.width());
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
		var $container = $("#" + containerId);
		
		$container.find(">.file-edit>.head a.tab").removeClass("active");
    	$container.find(">.file-edit>.head a.save").addClass("active");
    	
		$container.find(">.file-edit>.body>div.edit").hide();
		$container.find(">.file-edit>.body>.preview").hide();
		$container.find(">.file-edit>.body>.save").show();
		$(window).resize();
	},
	preview: function(containerId) {
		var $container = $("#" + containerId);
    	
		$container.find(">.file-edit>.head a.tab").removeClass("active");
    	$container.find(">.file-edit>.head a.preview").addClass("active");
    	
		$container.find(">.file-edit>.body>div.edit").hide();
		$container.find(">.file-edit>.body>.save").hide();
		$container.find(">.file-edit>.body>.preview").show();
		$(window).resize();
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
	}
}

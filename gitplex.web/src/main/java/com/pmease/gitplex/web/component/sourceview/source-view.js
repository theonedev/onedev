gitplex.sourceview = {
	init: function(codeId, fileContent, filePath, activeLine, ajaxIndicatorUrl, symbolQuery) {
		var $code = $("#" + codeId);
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
			highlightIdentifiers: {delay: 300},
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
		var editor = CodeMirror($code[0], options);
		
	    var modeInfo = CodeMirror.findModeByFileName(filePath);
	    if (modeInfo) {
		    editor.setOption("mode", modeInfo.mime);
			CodeMirror.autoLoadMode(editor, modeInfo.mode);
	    }
		
		var $sourceView = $code.closest(".source-view");
		$sourceView.on("autofit", function(event, width, height) {
			$sourceView.outerWidth(width);
			$sourceView.outerHeight(height);
			var $head = $sourceView.find(">.head");
			var $body = $sourceView.find(">.body");
			$body.outerHeight($sourceView.height()-$head.outerHeight());
			var $outline = $body.find(">.outline");
			if ($outline.is(":visible")) {
				$code.outerWidth($body.width()/4.0*3);
				$outline.outerWidth($body.width() - $code.outerWidth()-1);
				$outline.outerHeight($body.height());
				$code.find(">.CodeMirror").outerHeight($body.height());
			} else {
				$code.outerWidth($body.width());
				$code.find(">.CodeMirror").outerHeight($body.height());
			}
			editor.refresh();
		});
		
		var $outlineToggle = $sourceView.find(">.head .outline-toggle");
		var $outline = $sourceView.find(">.body>.outline");
		var cookieKey = "sourceView.outline";
		if ($outlineToggle.length != 0) {
			if (Cookies.get(cookieKey) === "no")
				$outline.hide();
			$outlineToggle.click(function() {
				$outline.toggle();
				if ($outline.is(":visible"))
					Cookies.set(cookieKey, "yes", {expires: Infinity});
				else 
					Cookies.set(cookieKey, "no", {expires: Infinity});
				$sourceView.trigger("autofit", [$sourceView.outerWidth(), $sourceView.outerHeight()]);
			});
		}
		
		gitplex.sourceview.gotoLine(editor, activeLine);
	}, 
	
	symbolsQueried: function(codeId, symbolsId) {
		var $symbols = $("#" + symbolsId);
		var $tooltip = $("#" + codeId + "-symbolstooltip");
		$tooltip.children().remove();
		$symbols.children().appendTo($tooltip);
		$tooltip.align();
	},
	
	gotoSymbol: function(outlineId, line) {
		var cm = $('#'+ outlineId).closest(".source-view").find(".CodeMirror")[0].CodeMirror;		
		gitplex.sourceview.gotoLine(cm, line);
	},
	
	gotoLine: function(cm, line) {
		cm.setCursor(line);
		var h = cm.getScrollInfo().clientHeight;
		var coords = cm.charCoords({line: line, ch: 0}, "local");
		cm.scrollTo(null, (coords.top + coords.bottom - h) / 2); 			
	}
	
}
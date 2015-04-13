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
	    editor.setOption("mode", modeInfo.mime);
		CodeMirror.autoLoadMode(editor, modeInfo.mode);
		
		var $sourceView = $code.closest(".source-view");
		$sourceView.resize(function() {
			var $head = $sourceView.find(">.head");
			var $body = $sourceView.find(">.body");
			var $outline = $body.find(">.outline");
			if ($outline.is(":visible")) {
				$code.width($body.width()/4.0*3);
				$outline.width($body.width() - $code.width()-1);
			} else {
				$code.width($body.width());
			}
			$outline.height($sourceView.height()-$head.height());
			$code.find(">.CodeMirror").height($outline.height());
		});
		
		$sourceView.resize();

		gitplex.sourceview.gotoLine(editor, activeLine);
	}, 
	
	symbolsQueried: function(codeId, symbolsId) {
		var $symbols = $("#" + symbolsId);
		var $tooltip = $("#" + codeId + "-symbolstooltip");
		$tooltip.children().remove();
		$symbols.children().appendTo($tooltip);
		$tooltip.align();
	},
	
	toggleOutline: function(outlineId) {
		var $outline = $("#" + outlineId);
		$outline.toggle();
		$outline.closest(".source-view").resize();
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
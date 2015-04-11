gitplex.sourceview = {
	init: function(codeContainerId, fileContent, filePath, ajaxIndicatorUrl, symbolQuery) {
		/*
		var editor = ace.edit(codeId);
		var modeList = ace.require("ace/ext/modelist");
		editor.setTheme("ace/theme/eclipse");
		editor.setReadOnly(true);
		editor.getSession().setMode(modeList.getModeForPath(filePath).mode);
		editor.getSession().selection.on("changeCursor", function(e) {
		}); 		
		*/
		
		var $codeContainer = $("#" + codeContainerId);
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
					$tooltip.attr("id", codeContainerId + "-symbolstooltip");
					symbolQuery($(tokenEl).text());
					return tooltip;
				} 
			},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		};
		var editor = CodeMirror($codeContainer[0], options);
		
	    var modeInfo = CodeMirror.findModeByFileName(filePath);
	    editor.setOption("mode", modeInfo.mime);
		CodeMirror.autoLoadMode(editor, modeInfo.mode);
	}, 
	
	symbolsQueried: function(codeContainerId, symbolsContainerId) {
		var $symbolsContainer = $("#" + symbolsContainerId);
		var $tooltipContainer = $("#" + codeContainerId + "-symbolstooltip");
		$tooltipContainer.children().remove();
		$symbolsContainer.children().appendTo($tooltipContainer);
		$tooltipContainer.align();
	}
}
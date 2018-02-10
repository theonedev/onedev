turbodev.server.highlight = function($container) {
	$container.find("pre>code").each(function() {
		var $this = $(this);
		$this.parent().addClass("highlight");
		var text = $this.text().trim();
		$this.empty();
		var cm = CodeMirror(this, {
			readOnly: turbodev.server.isDevice()?"nocursor":true,
			value: text, 
			highlightIdentifiers: {delay: 500},
			theme: "eclipse",
			scrollbarStyle: "simple",
			matchBrackets: true
		});
		var modeHint = $this.attr("class");
		if (modeHint) {
			if (modeHint.indexOf("language-") == 0)
				modeHint = modeHint.substring("language-".length);
			var modeInfo = CodeMirror.findModeByExtension(modeHint);
			if (!modeInfo)
				modeInfo = CodeMirror.findModeByName(modeHint);
		    if (modeInfo) {
		    	cm.setOption("mode", modeInfo.mime);
				CodeMirror.autoLoadMode(cm, modeInfo.mode);
		    }
		}
	});
};
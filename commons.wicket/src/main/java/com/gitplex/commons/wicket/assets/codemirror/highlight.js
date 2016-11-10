gitplex.commons.highlight = function($container) {
	$container.find("pre>code").each(function() {
		var $this = $(this);
		$this.parent().addClass("highlight");
		var text = $this.text().trim();
		$this.empty();
		var cm = CodeMirror(this, {
			readOnly: "nocursor",
			value: text, 
			highlightIdentifiers: {delay: 500},
			theme: "eclipse",
			matchBrackets: true
		});
		var modeHint = $this.attr("class");
		if (modeHint) {
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
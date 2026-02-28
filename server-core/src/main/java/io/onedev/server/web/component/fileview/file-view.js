onedev.server.fileView = {
	onDomReady: function(containerId, fileName, fileContent) {
		var $container = $("#" + containerId);
		var $code = $container.find(".file-view-code");

		$container.css({display: "flex", flexDirection: "column", padding: 0, overflow: "hidden"});
		$code.css({flexGrow: 1, minHeight: 0});

		var cm = CodeMirror($code[0], {
			value: fileContent,
			readOnly: true,
			theme: "eclipse",
			lineNumbers: true,
			matchBrackets: true,
			foldGutter: true,
			scrollbarStyle: "simple",
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});

		cm.setSize(null, "100%");

		var hasConflicts = /^<{7}\s/m.test(fileContent);
		if (hasConflicts) {
			onedev.server.codemirror.setConflictAwareModeByFileName(cm, fileName);
		} else {
			onedev.server.codemirror.setModeByFileName(cm, fileName);
		}
	}
};

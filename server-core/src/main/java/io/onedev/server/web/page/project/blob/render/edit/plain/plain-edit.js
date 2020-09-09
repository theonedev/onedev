onedev.server.plainEdit = {
	onDomReady: function(containerId, fileName) {
		var $container = $("#" + containerId);
		var $plainEdit = $container.children(".plain-edit");
		var $source = $plainEdit.children(".source");
		var cm = CodeMirror($source[0], {
			value: $plainEdit.children(".input").val(),
			theme: "eclipse",
			indentWithTabs: false,
			indentUnit: 4,
			lineNumbers: true,
			lineWrapping: true,
			styleActiveLine: true,
			styleSelectedText: true,
			foldGutter: true,
			matchBrackets: true,
			scrollbarStyle: "simple",
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
			highlightIdentifiers: {delay: 500}
		});
		
		onedev.server.codemirror.setModeByFileName(cm, fileName);
		
		/*
		 * AreYouSure can not track dirty correctly for CodeMirror generated
		 * textarea, and it will always mark the form as clean even if we 
		 * mark it as dirty explicitly. So we use no-dirtytrack class to 
		 * disable automatic dirty track
		 */
		$plainEdit.closest("form").find("textarea").addClass("no-dirtytrack").addClass("no-autosize");
		
		cm.on("change", function() {
			$plainEdit.closest("form").addClass("dirty");
        });
        
		onedev.server.codemirror.bindShortcuts(cm);

        $plainEdit.on("beforeSubmit", function() {
            $plainEdit.children("textarea").val($plainEdit.find(">.source>.CodeMirror")[0].CodeMirror.getValue());
        });

		$source.on("resized", function() {
			setTimeout(function() {
				cm.setSize("100%", $source.height());
			});
			return false;
		});
	}
};

onedev.server.plainEdit = {
	onDomReady: function(containerId) {
		var $container = $("#" + containerId);
		var $plainEdit = $container.children(".plain-edit");
		var $source = $plainEdit.children(".source");
		var cm = CodeMirror($source[0], {
			value: $plainEdit.children(".input").val(),
			theme: "eclipse",
			indentWithTabs: true,
			indentUnit: 4,
			lineNumbers: true,
			lineWrapping: true,
			tabSize: 4,
			styleActiveLine: true,
			styleSelectedText: true,
			foldGutter: true,
			matchBrackets: true,
			scrollbarStyle: "simple",
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
			highlightIdentifiers: {delay: 500}
		});
		
		onedev.server.codemirror.setMode(cm, "buildspec.xml");
		
		/*
		 * AreYouSure can not track dirty correctly for CodeMirror generated
		 * textarea, and it will always mark the form as clean even if we 
		 * mark it as dirty explicitly. So we use no-dirtytrack class to 
		 * disable automatic dirty track
		 */
		$plainEdit.closest("form").find("textarea").addClass("no-dirtytrack");
		
		cm.on("change", function() {
			$plainEdit.closest("form").addClass("dirty");
        });
        
		onedev.server.codemirror.bindShortcuts(cm);
		
		$plainEdit.on("autofit", function(e, width, height) {
			if (cm.getOption("fullScreen"))
				return;
			
			$plainEdit.outerWidth(width);
			$plainEdit.outerHeight(height);
            cm.setSize($plainEdit.width(), $plainEdit.height());
            cm.refresh();
        });
	},
	onSubmit: function(containerId) {
		var cm = $("#" + containerId + ">.plain-edit>.source>.CodeMirror")[0].CodeMirror;	
		$("#" + containerId + ">.plain-edit>textarea").val(cm.getValue());
	}
};

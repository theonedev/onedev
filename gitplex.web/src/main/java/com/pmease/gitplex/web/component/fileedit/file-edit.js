gitplex.fileEdit = {
	init: function(containerId, filePath, fileContent, previewCallback, saveCallback) {
		var $container = $("#" + containerId);
		var $code = $container.find(">file-edit>.body>.edit");
		var cm = CodeMirror($code[0], {
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
		
		setMode(cm, filePath);
		
	    $container.find(">file-edit>.head>.edit").click(function() {
			$container.find(">.file-edit>.body>.preview").hide();
			$container.find(">.file-edit>.body>.save").hide();
			$container.find(">.file-edit>.body>.edit").show();
	    });
	    $container.find(">.file-edit>.head>.preview").click(function() {
	    	previewCallback($cm.getValue());
	    });
	    $container.find(">.file-edit>.head>.save").click(function() {
	    	saveCallback($cm.getValue());
	    });
	},
	save: function(containerId) {
		var $container = $("#" + containerId);
		$container.find(">.file-edit>.body>.edit").hide();
		$container.find(">.file-edit>.body>.preview").hide();
		$container.find(">.file-edit>.body>.save").show();
	},
	preview: function(containerId) {
		var $container = $("#" + containerId);
		$container.find(">.file-edit>.body>.edit").hide();
		$container.find(">.file-edit>.body>.save").hide();
		$container.find(">.file-edit>.body>.preview").show();
	},
	setMode: function(cm, filePath) {
		if (typeof cm === "string") 
			cm = $("#"+ cm + ">.file-edit>.body>.edit>.CodeMirror")[0].CodeMirror;		

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

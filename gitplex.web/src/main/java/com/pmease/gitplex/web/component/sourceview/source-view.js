gitplex.sourceview = {
	init: function(codeId, fileContent, filePath) {
		/*
		var editor = ace.edit(codeId);
		var modeList = ace.require("ace/ext/modelist");
		editor.setTheme("ace/theme/eclipse");
		editor.setReadOnly(true);
		editor.getSession().setMode(modeList.getModeForPath(filePath).mode);
		editor.getSession().selection.on("changeCursor", function(e) {
		}); 		
		*/
		
		var $code = $("#" + codeId);
		var options = {
			value: fileContent, 
			readOnly: false,
			theme: "eclipse",
			lineNumbers: true,
			lineWrapping: true,
			styleActiveLine: true,
			styleSelectedText: true,
			foldGutter: true,
			matchBrackets: true,
			scrollbarStyle: "simple",
			highlightIdentifiers: {delay: 300},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		};
		var editor = CodeMirror($code[0], options);
		
	    var modeInfo = CodeMirror.findModeByFileName(filePath);
	    editor.setOption("mode", modeInfo.mime);
		CodeMirror.autoLoadMode(editor, modeInfo.mode);
	}
}
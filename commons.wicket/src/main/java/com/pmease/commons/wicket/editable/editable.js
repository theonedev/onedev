function adjustReflectionEditor(editorId) {
	var editor = $("#" + editorId);
	var input = editor.find(".value>input[type=checkbox]");
	input.parent().prev("label.name").addClass("pull-left");
	input.after("<div style='clear:both;'/>")
	
	input = editor.find(".value>div>input[type=checkbox]");
	input.parent().parent().prev("label.name").addClass("pull-left");
	input.after("<div style='clear:both;'/>")
}
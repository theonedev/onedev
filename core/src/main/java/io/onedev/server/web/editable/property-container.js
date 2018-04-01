onedev.server.propertyContainer = {
	onDomReady: function(containerId) {
		var $container = $("#" + containerId);
		var input = $container.find(".value>input[type=checkbox]");
		input.parent().prev("label.name").addClass("pull-left");
		input.after("<div style='clear:both;'/>")
		
		input = $container.find(".value>div>input[type=checkbox]");
		input.parent().parent().prev("label.name").addClass("pull-left");
		input.after("<div style='clear:both;'/>")
	}
}
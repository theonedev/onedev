onedev.server.propertyContainer = {
	onDomReady: function(containerId) {
		var $container = $("#" + containerId);
		var $input = $container.find(".value>input[type=checkbox]");
		$input.css("margin-right", "8px");
		$input.css("vertical-align", "text-top");
		var $label = $input.parent().prev("label.name");
		$label.attr("for", $input.attr("id")).insertAfter($input);
		
		$input = $container.find(".value>div>input[type=checkbox]");
		$input.css("margin-right", "8px");
		$input.css("vertical-align", "text-top");
		$label = $input.parent().parent().prev("label.name");
		$label.attr("for", $input.attr("id")).insertAfter($input);
	}
}
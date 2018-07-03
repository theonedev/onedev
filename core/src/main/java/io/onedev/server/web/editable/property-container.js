onedev.server.propertyContainer = {
	onDomReady: function(containerId) {
		var $container = $("#" + containerId);
		$container.find(".value>input[type=checkbox]").each(function() {
			var $input = $(this);
			$input.css("margin-right", "8px");
			$input.css("vertical-align", "text-top");
			var $label = $input.parent().prev("label.name");
			$label.attr("for", $input.attr("id")).insertAfter($input);
		});
		
		$container.find(".value>div>input[type=checkbox]").each(function() {
			var $input = $(this);
			$input.css("margin-right", "8px");
			$input.css("vertical-align", "text-top");
			$label = $input.parent().parent().prev("label.name");
			$label.attr("for", $input.attr("id")).insertAfter($input);
		});
	}
}
onedev.server.confirm = function(containerId, confirmInput) {
	var $container = $("#" + containerId);
	var $confirmBtn = $container.find(".btn-danger, .btn-primary");
	if (confirmInput) {
		$confirmBtn.attr("disabled", "disabled");
		$container.find("input.confirm").on("input", function() {
			if ($(this).val() === confirmInput)
				$confirmBtn.removeAttr("disabled");
			else
				$confirmBtn.attr("disabled", "disabled");
		});
	}
};
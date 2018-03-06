onedev.server.confirmDelete = function(containerId, confirmText) {
	var $container = $("#" + containerId);
	
	$container.find("input.confirm").on("input", function() {
		var $deleteBtn = $container.find(".btn-danger");
		if ($(this).val() === confirmText)
			$deleteBtn.removeAttr("disabled");
		else
			$deleteBtn.attr("disabled", "disabled");
	});
};
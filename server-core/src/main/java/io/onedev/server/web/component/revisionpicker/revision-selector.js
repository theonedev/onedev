onedev.server.revisionSelector = {
	onDomReady: function(containerId) {
		var $container = $("#" + containerId);
		var $floating = $container.closest(".floating");
		$floating.on("open click", function() {
			$container.find("input").focus();
		});
		$container.find("input").selectByTyping($container);
	}
};
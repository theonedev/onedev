onedev.server.refreshToken = {
	onGenerate: function(containerId, targetUrl, state, callback) {
		var width = 900;
		var height = 600;
		var left = (screen.width - width) / 2;
        var top = (screen.height - height) / 4;
		window.name = state;
		window.callback = callback;
		var popup = window.open(targetUrl, "_blank", "left=" + left + ",top=" + top + ",width=" + width + ",height=" + height + ",resizable=yes,scrollbars=yes");
		$("#" + containerId).data("popup", popup);
	},
	onGenerated: function(containerId, refreshToken) {
		var $container = $("#" + containerId);
		$container.data("popup").close();
		if (refreshToken) {
			$container.find("input").val(refreshToken);
			onedev.server.form.markDirty($container.closest("form"));
		}
	}
}
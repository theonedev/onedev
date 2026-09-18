onedev.server.refreshToken = {
	onGenerate: function(containerId, targetUrl, state, callback) {
		var $container = $("#" + containerId);
		var previousChannel = $container.data("oauthChannel");
		if (previousChannel)
			previousChannel.close();
		// COOP can isolate the OAuth popup from its opener during navigation.
		var channel = new BroadcastChannel("onedev-refresh-token-" + state);
		channel.onmessage = function(event) {
			channel.close();
			$container.removeData("oauthChannel");
			if (event.data)
				callback();
		};
		$container.data("oauthChannel", channel);
		var width = 900;
		var height = 600;
		var left = (screen.width - width) / 2;
        var top = (screen.height - height) / 4;
		window.open(targetUrl, "_blank", "left=" + left + ",top=" + top + ",width=" + width + ",height=" + height + ",resizable=yes,scrollbars=yes");
	},
	onGenerated: function(containerId, refreshToken) {
		var $container = $("#" + containerId);
		if (refreshToken) {
			$container.find("input").val(refreshToken);
			onedev.server.form.markDirty($container.closest("form"));
		}
	}
}

onedev.server.test = {
	onDomReady: function() {
	},
	openTerminal: function() {
		var width = 900;
		var height = 600;
		var left = (screen.width - width) / 2;
        var top = (screen.height - height) / 4;
		window.open("/terminal/5", "_blank", "left=" + left + ",top=" + top + ",width=" + width + ",height=" + height + ",resizable=yes");
	}
}

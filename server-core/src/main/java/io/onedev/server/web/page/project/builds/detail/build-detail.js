onedev.server.buildDetail = {
	openTerminal: function(terminalUrl) {
		var width = 900;
		var height = 600;
		var left = (screen.width - width) / 2;
        var top = (screen.height - height) / 4;
		window.open(terminalUrl, "_blank", "left=" + left + ",top=" + top + ",width=" + width + ",height=" + height + ",resizable=yes");
	}
}
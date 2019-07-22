onedev.server.copyClipboard = {
	onDomReady: function(buttonId, text) {
		var $button = $("#" + buttonId);
		var clipboard = new Clipboard("#"+buttonId, {
			text: function(trigger) {
				return text;
			}
		});
		$button.attr("title", "Copy");
		$button.addClass("copy-clipboard");
	}
};
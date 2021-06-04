onedev.server.copyToClipboard = {
	onDomReady: function(buttonId, text) {
		var $button = $("#" + buttonId);
		new Clipboard("#"+buttonId, {text: function() {return text;}});
		$button.attr("title", "Copy to clipboard");
		$button.addClass("pressable");
	}
};
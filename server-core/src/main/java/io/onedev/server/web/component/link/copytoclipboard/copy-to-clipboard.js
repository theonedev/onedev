onedev.server.copyToClipboard = {
	onDomReady: function(buttonId, text) {
		var $button = $("#" + buttonId);
		var options = {
			text: function() {
				return text;
			}
		};
		var $modal = $button.closest(".modal-dialog");
		if ($modal.length != 0) 
			options.container = $modal[0];
		new ClipboardJS("#"+buttonId, options);
		$button.attr("title", "Copy to clipboard");
		$button.addClass("pressable");
	}
};
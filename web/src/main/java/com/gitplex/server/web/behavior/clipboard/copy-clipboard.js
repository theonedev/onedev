gitplex.server.copyclipboard = {
	init: function(buttonId, text) {
		var $button = $("#" + buttonId);
		var clipboard = new Clipboard("#"+buttonId, {
			text: function(trigger) {
				return text;
			}
		});
		$button.tooltip({animation: false, delay: 0, title: "Copy"});
		clipboard.on("success", function(e) {
			$button.tooltip("destroy");
			$button.tooltip({animation: false, delay: 0, title: "Copied"}).tooltip("show");
			$button.on("mouseout", function() {
				$button.tooltip("destroy").tooltip({title: "Copy", animation: false, delay: 0});
				$button.off("mouseout");
			});
		});
	}
};
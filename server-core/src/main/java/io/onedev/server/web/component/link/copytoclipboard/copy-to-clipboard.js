onedev.server.copyToClipboard = {
	onDomReady: function(buttonId, text, tooltip, copiedTooltip) {
		var $button = $("#" + buttonId);
		onedev.server.copyToClipboard.init($button, function() {
			return text;
		}, tooltip, copiedTooltip);
	},
	init: function($button, text, tooltip, copiedTooltip) {
		var options = {
			text: text
		};
		var $iconUse = $button.find("svg use").first();
		var iconHref = $iconUse.attr("xlink:href");
		var $icon = $iconUse.closest("svg");
		var iconRotated = $icon.hasClass("rotate-45");
		var $modal = $button.closest(".modal-dialog");
		if ($modal.length != 0) 
			options.container = $modal[0];
		var clipboard = new ClipboardJS($button[0], options);
		clipboard.on("success", function(e) {
			clearTimeout(e.trigger.copyFeedbackTimeout);
			if ($iconUse.length != 0) {
				$iconUse.attr("xlink:href", onedev.server.icons + "#tick");
				$icon.removeClass("rotate-45");
			}
			var tooltipInstance = e.trigger._tippy;
			if (tooltipInstance) {
				tooltipInstance.setContent(copiedTooltip);
				tooltipInstance.show();
			}
			e.trigger.copyFeedbackTimeout = setTimeout(function() {
				if ($iconUse.length != 0) {
					$iconUse.attr("xlink:href", iconHref);
					if (iconRotated)
						$icon.addClass("rotate-45");
				}
				if (tooltipInstance && !tooltipInstance.state.isDestroyed) {
					tooltipInstance.hide();
					tooltipInstance.setContent(tooltip);
				}
			}, 1000);
		});
		$button.attr("data-tippy-content", tooltip);
	}
};

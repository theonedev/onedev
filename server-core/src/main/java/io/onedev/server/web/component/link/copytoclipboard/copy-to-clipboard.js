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
		var $modal = $button.closest(".modal");
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
	},
	initCodeSpans: function($container, tooltip, copiedTooltip) {
		$container.find("code").addBack("code").each(function() {
			var $code = $(this);
			if ($code.closest("pre").length != 0 || $code.hasClass("code-span"))
				return;

			$code.addClass("code-span");
			$code.attr("data-tippy-content", tooltip);

			var dragStart = null;
			var dragged = false;
			$code.on("mousedown", function(e) {
				dragStart = {x: e.clientX, y: e.clientY};
				dragged = false;
			}).on("mousemove", function(e) {
				if (dragStart && (Math.abs(e.clientX - dragStart.x) > 4 || Math.abs(e.clientY - dragStart.y) > 4))
					dragged = true;
			}).on("click", function(e) {
				if (dragged)
					return;
				var selection = window.getSelection();
				if (selection && selection.toString().length > 0)
					return;

				e.preventDefault();
				e.stopPropagation();

				var text = $code.text();
				var showCopied = function() {
					$code.addClass("copied");
					var tooltipInstance = $code[0]._tippy;
					if (tooltipInstance) {
						clearTimeout($code[0].copyFeedbackTimeout);
						$code.off("mouseleave.copyFeedback");
						tooltipInstance.enable();
						tooltipInstance.setContent(copiedTooltip);
						tooltipInstance.show();
						$code[0].copyFeedbackTimeout = setTimeout(function() {
							if (!tooltipInstance.state.isDestroyed) {
								tooltipInstance.hide();
								if ($code.is(":hover")) {
									tooltipInstance.disable();
									$code.one("mouseleave.copyFeedback", function() {
										if (!tooltipInstance.state.isDestroyed) {
											tooltipInstance.setContent(tooltip);
											tooltipInstance.enable();
										}
									});
								} else {
									tooltipInstance.setContent(tooltip);
								}
							}
						}, 1000);
					}
					setTimeout(function() {
						$code.removeClass("copied");
					}, 150);
				};
				var copyWithClipboardJs = function() {
					var options = {
						text: function() {
							return text;
						}
					};
					var $modal = $code.closest(".modal");
					if ($modal.length != 0)
						options.container = $modal[0];

					var $triggerContainer = $modal.length != 0 ? $modal : $(document.body);
					var $trigger = $("<button></button>").css({
						position: "fixed",
						left: "-9999px"
					}).appendTo($triggerContainer);
					var clipboard = new ClipboardJS($trigger[0], options);
					clipboard.on("success", function() {
						clipboard.destroy();
						$trigger.remove();
						showCopied();
					});
					clipboard.on("error", function() {
						clipboard.destroy();
						$trigger.remove();
					});
					$trigger[0].click();
				};
				if (navigator.clipboard && window.isSecureContext) {
					navigator.clipboard.writeText(text).then(showCopied).catch(copyWithClipboardJs);
				} else {
					copyWithClipboardJs();
				}
			});
		});
	}
};

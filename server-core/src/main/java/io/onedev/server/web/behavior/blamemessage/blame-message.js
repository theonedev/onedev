onedev.server.blameMessage = {
	show: function(tooltipId, authoring, authoringTitle, message) {
		var $blameTooltip = $("#" + tooltipId);
		$blameTooltip.empty();
		if (authoring) {
			$blameTooltip.html("<div class='authoring'></div><div class='message'></div>");
			var $authoring = $blameTooltip.children(".authoring");
			$authoring.text(authoring).attr("data-tippy-content", authoringTitle);
			tippy($authoring[0], {
				delay: [500, 0],
				placement: "auto"
			});
			$blameTooltip.children(".message").text(message);
		} else {
			$blameTooltip.text(message);
		}
		$blameTooltip.align({placement: $blameTooltip.data("alignment"), target: {element: $blameTooltip.data("trigger")}});
	}
};

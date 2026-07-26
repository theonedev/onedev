onedev.server.blameMessage = {
	show: function(tooltipId, authoring, authoringTitle, message) {
		var $blameTooltip = $("#" + tooltipId);
		$blameTooltip.empty();
		if (authoring) {
			$blameTooltip.html("<div class='authoring'></div><div class='message'></div>");
			$blameTooltip.children(".authoring").text(authoring);
			$blameTooltip.children(".authoring").attr("title", authoringTitle);
			$blameTooltip.children(".message").text(message);
		} else {
			$blameTooltip.text(message);
		}
		$blameTooltip.align({placement: $blameTooltip.data("alignment"), target: {element: $blameTooltip.data("trigger")}});
	}
};

onedev.server.stateStats = {
	onDomReady: function(containerId) {
		var $stateStats = $("#" + containerId + ">.state-stats");
		function calcWidth() {
			$stateStats.children("a").width("auto").each(function() {
				var totalWidth = $(this).parent().width();
				var percent = $(this).data("percent");
				var width = totalWidth*percent;
				if (width < 4)
					width = 4;
				$(this).width(width);
			});
			$stateStats.children("span").outerWidth("100%");
		}
		$stateStats.on("resized", calcWidth);
		calcWidth();
	}
};

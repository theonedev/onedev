gitplex.symboltooltip = {
	init: function(containerId, queryCallback, ajaxIndicatorUrl) {
		var container = document.getElementById(containerId);
		container.query = function(revision, $symbol) {
			var $tooltip = $("<div class='symbol-tooltip'><img src=" + ajaxIndicatorUrl + "></img></div>");
			var tooltip = $tooltip[0];
			document.body.appendChild(tooltip);
			
			tooltip.alignment = {x: 0, y:0, offset:2, showIndicator: false, target: {element: $symbol[0], x: 0, y: 100}};
			$tooltip.align();
			
			queryCallback(revision, $symbol.text());
			
			return $tooltip;
		};
	},
	doneQuery: function(contentId) {
		$(".symbol-tooltip").empty().append($("#" + contentId).children()).align();
	}
}
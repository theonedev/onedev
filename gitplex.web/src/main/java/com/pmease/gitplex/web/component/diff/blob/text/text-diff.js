gitplex.textdiff = {
	init: function(containerId, symbolTooltipId, oldRev, newRev) {
		var $container = $("#" + containerId);
		var $symbols = $container.find(".cm-property, .cm-variable, .cm-variable-2, .cm-variable-3, .cm-def, .cm-meta"); 
		$symbols.mouseover(function() {
			var revision;
			var $symbol = $(this);
			if ($symbol.hasClass("delete")) {
				revision = oldRev;
			} else {
				var $td = $symbol.closest("td");
				
				// if is deleted line or if it is on left side of split view
				if ($td.hasClass("old") && !$td.hasClass("new") || $td.next().is("td"))
					revision = oldRev;
				else
					revision = newRev;
			}
			var symbolTooltip = document.getElementById(symbolTooltipId);
			if (symbolTooltip.onMouseOverSymbol)
				symbolTooltip.onMouseOverSymbol(revision, this);
		});
		$container.find("td.content").mouseover(function() {
			if ($(this).hasClass("left")) {
				$container.find("td.content.right").addClass("noselect");
				$container.find("td.content.left").removeClass("noselect");
			} else if ($(this).hasClass("right")) {
				$container.find("td.content.left").addClass("noselect");
				$container.find("td.content.right").removeClass("noselect");
			}
		});
	}
}

onedev.server.savedQueries = {
	breakpoint: 1200,

	showDropdown: function(link) {
		if (window.innerWidth >= this.breakpoint)
			return true;

		var $link = $(link);
		var $floating = $(".saved-queries-dropdown").filter(function() {
			return $(this).data("trigger") && $(this).data("trigger")[0] === link;
		});
		if ($floating.length) {
			onedev.server.floating.close($floating.attr("id"));
			return false;
		}

		var $panel = $link.closest(".side-main").children(".saved-queries-panel");
		if (!$panel.length)
			return true;

		var floatingId = "saved-queries-dropdown-" + Date.now();
		var $placeholder = $("<span class='saved-queries-placeholder'></span>");
		$panel.before($placeholder);
		$floating = $("<div class='floating saved-queries-dropdown'></div>")
			.attr("id", floatingId)
			.appendTo(document.body)
			.append($panel);
		$floating.data("trigger", $link);
		$floating.on("close", function() {
			var $currentPanel = $(this).children(".saved-queries-panel");
			if ($currentPanel.length)
				$placeholder.before($currentPanel);
			$placeholder.remove();
		});

		var alignment = {
			target: {element: link},
			placement: {x: 0, y: 0, targetX: 0, targetY: 100, offset: 4}
		};
		onedev.server.floating.init(floatingId, alignment, false, false, undefined,
			function() { onedev.server.floating.close(floatingId); });
		$(document).trigger("afterElementReplace");
		return false;
	},

	closeDropdown: function(link) {
		var $floating = $(link).closest(".saved-queries-dropdown");
		if ($floating.length && window.innerWidth < this.breakpoint) {
			onedev.server.floating.close($floating.attr("id"));
			return false;
		}
		return true;
	}
};

$(window).on("resize", function() {
	if (window.innerWidth >= onedev.server.savedQueries.breakpoint) {
		$(".saved-queries-dropdown").each(function() {
			onedev.server.floating.close(this.id);
		});
	}
});

onedev.server.revisionSelector = {
	init: function(containerId, callback) {
		var $container = $("#" + containerId);
		$container.data("callback", callback);
		var $floating = $container.closest(".floating");
		$floating.on("open click", function() {
			$container.find("input").focus();
		});
		onedev.server.revisionSelector.bindInputKeys(containerId);
	},
	bindInputKeys: function(containerId) {
		var $container = $("#" + containerId);
		var callback = $container.data("callback");
		var $input = $container.find("input");
		$input.bind("keydown", "return", function() {
			var $active = $container.find("ul.items li.active");
			if ($active.length != 0)
				callback($active.data("value"));
			else
				callback();
		});
		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("ul.items li.active");
			var $prev = $active.prev("li:not(.loading-indicator)");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			}
			$container.find("ul.items li.active").scrollIntoView();
		});
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("ul.items li.active");
			var $next = $active.next("li:not(.loading-indicator)");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			}
			$container.find("ul.items li.active").scrollIntoView();
		});
	}
};
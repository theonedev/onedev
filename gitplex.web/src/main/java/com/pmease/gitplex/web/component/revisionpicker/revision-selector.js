gitplex.revisionSelector = {
	init: function(containerId, callback) {
		var $container = $("#" + containerId);
		var $floating = $container.closest(".floating");
		$floating.on("open click", function() {
			$container.find("input").focus();
		});
		gitplex.revisionSelector.bindInputKeys(containerId, callback);
		$container.find("ul.refs").scroll(function() {
			if($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
				callback("load");
			}
		});
	},
	bindInputKeys: function(containerId, callback) {
		var $container = $("#" + containerId);
		var $input = $container.find("input");
		$input.bind("keydown", "return", function() {
			var $active = $container.find("ul.refs li.active");
			if ($active.length != 0)
				callback("return", $active.data("value"));
			else
				callback("return");
		});
		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("ul.refs li.active");
			var $prev = $active.prev("li");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			}
			$container.find("ul.refs").scrollIntoView("li.active", 8, 8);
		});
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("ul.refs li.active");
			var $next = $active.next("li");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			}
			$container.find("ul.refs").scrollIntoView("li.active", 8, 8);
		});
	}
};
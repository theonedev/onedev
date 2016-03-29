gitplex.depotSelector = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);

		$input.closest(".floating").on("open", function() {
			$input.focus();
		});
		
		var $container = $input.closest(".depot-selector");
		
		$input.bind("keydown", "return", function() {
			var $id = $container.find("li.depot.active input");
			if ($id.length != 0)
				callback($id.val());
		});

		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("li.depot.active");
			var $prev = $active.prev("li.depot");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			} else {
				$prev = $active.closest("li.account").prev("li.account");
				if ($prev.length != 0) {
					$active.removeClass("active");
					$prev.find("li.depot:last-child").addClass("active");
				}
			}
			$container.find(".accounts").scrollIntoView("li.depot.active", 30, 8);
		});
		
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("li.depot.active");
			var $next = $active.next("li.depot");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			} else {
				$next = $active.closest("li.account").next("li.account");
				if ($next.length != 0) {
					$active.removeClass("active");
					$next.find("li.depot:first-child").addClass("active");
				}
			}
			$container.find(".accounts").scrollIntoView("li.depot.active", 30, 8);
		});
	}
}
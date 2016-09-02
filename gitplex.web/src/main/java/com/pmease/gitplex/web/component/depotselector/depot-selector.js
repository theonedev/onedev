gitplex.depotSelector = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);

		$input.closest(".floating").on("open", function() {
			$input.focus();
		});
		
		var $container = $input.closest(".depot-selector");
		
		$input.bind("keydown", "return", function() {
			var id = $container.find("li.depot.active").data("id");
			if (id)
				callback(id);
		});

		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("li.depot.active");
			var $prev = $active.prev("li.depot");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			}
			$container.find(".depots").scrollIntoView("li.depot.active", 30, 8);
		});
		
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("li.depot.active");
			var $next = $active.next("li.depot");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			} 
			$container.find(".depots").scrollIntoView("li.depot.active", 30, 8);
		});
	}
};
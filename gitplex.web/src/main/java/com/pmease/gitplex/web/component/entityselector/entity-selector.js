gitplex.entitySelector = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);

		$input.closest(".floating").on("open", function() {
			$input.focus();
		});
		
		var $container = $input.closest(".entity-selector");
		
		$input.bind("keydown", "return", function() {
			var id = $container.find("li.entity.active").data("id");
			if (id)
				callback(id);
		});

		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("li.entity.active");
			var $prev = $active.prev("li.entity");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			}
			$container.find(".entities").scrollIntoView("li.entity.active", 30, 8);
		});
		
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("li.entity.active");
			var $next = $active.next("li.entity");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			} 
			$container.find(".entities").scrollIntoView("li.entity.active", 30, 8);
		});
	}
}
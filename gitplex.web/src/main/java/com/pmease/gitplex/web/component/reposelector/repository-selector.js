gitplex.repositorySelector = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);

		$input.closest(".floating").on("open", function() {
			$input.focus();
		});
		
		var $container = $input.closest(".repository-selector");
		
		$input.bind("keydown", "return", function() {
			var $id = $container.find("li.repository.active input");
			if ($id.length != 0)
				callback($id.val());
		});

		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("li.repository.active");
			var $prev = $active.prev("li.repository");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			} else {
				$prev = $active.closest("li.account").prev("li.account");
				if ($prev.length != 0) {
					$active.removeClass("active");
					$prev.find("li.repository:last-child").addClass("active");
				}
			}
			$container.find(".accounts").scrollIntoView("li.repository.active", 30, 8);
		});
		
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("li.repository.active");
			var $next = $active.next("li.repository");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			} else {
				$next = $active.closest("li.account").next("li.account");
				if ($next.length != 0) {
					$active.removeClass("active");
					$next.find("li.repository:first-child").addClass("active");
				}
			}
			$container.find(".accounts").scrollIntoView("li.repository.active", 30, 8);
		});
	}
}
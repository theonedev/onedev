gitplex.accountSelector = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);

		$input.closest(".floating").on("open", function() {
			$input.focus();
		});
		
		var $container = $input.closest(".account-selector");
		
		$input.bind("keydown", "return", function() {
			var id = $container.find("li.account.active").data("id");
			if (id)
				callback(id);
		});

		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("li.account.active");
			var $prev = $active.prev("li.account");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			}
			$container.find(".accounts").scrollIntoView("li.account.active", 30, 8);
		});
		
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("li.account.active");
			var $next = $active.next("li.account");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			} 
			$container.find(".accounts").scrollIntoView("li.account.active", 30, 8);
		});
	}
}
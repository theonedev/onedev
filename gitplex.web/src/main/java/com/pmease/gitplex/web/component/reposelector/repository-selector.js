gitplex.repositorySelector = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);

		$input.closest(".dropdown-panel").on("show", function() {
			$input.focus();
		});
		$input.focus();
		
		var $container = $input.closest(".repository-selector");
		
		$input.bind("keydown", "return", function() {
			var $id = $container.find("li.repository.active input");
			if ($id.length != 0)
				callback($id.val());
		});

		function scrollIfNecessary() {
			var margin = 30;
			var $active = $container.find("li.repository.active");
			var $accounts = $container.find(".accounts");
			var contentTop = $accounts.offset().top;
			var activeTop = $active.offset().top;
			if (activeTop-margin<contentTop)
				$accounts.scrollTop($accounts.scrollTop()-(contentTop-activeTop+margin));
			var contentBottom = contentTop + $accounts.height();
			var activeBottom = activeTop + $active.height();
			if (activeBottom+margin>contentBottom) 
				$accounts.scrollTop($accounts.scrollTop()+(activeBottom+margin-contentBottom));
		};
		
		$input.bind("keydown", "up", function() {
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
			scrollIfNecessary();
		});
		
		$input.bind("keydown", "down", function() {
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
			scrollIfNecessary();
		});
	}
}
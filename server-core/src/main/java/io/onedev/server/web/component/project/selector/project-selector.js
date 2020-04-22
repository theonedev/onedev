onedev.server.projectSelector = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);

		$input.closest(".floating").on("open", function() {
			$input.focus();
		});
		
		var $container = $input.closest(".project-selector");
		
		$input.bind("keydown", "return", function() {
			var id = $container.find("li.project.active").data("id");
			if (id)
				callback(id);
		});

		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("li.project.active");
			var $prev = $active.prev("li.project");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
			}
			$("li.project.active").scrollIntoView();
		});
		
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("li.project.active");
			var $next = $active.next("li.project");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
			} 
			$("li.project.active").scrollIntoView();
		});
	}
};
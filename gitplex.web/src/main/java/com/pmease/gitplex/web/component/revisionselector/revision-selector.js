gitplex.revisionSelector = {
	init: function(containerId, callback) {
		var $container = $("#" + containerId);
		$container.closest(".dropdown-panel").on("show", function() {
			$container.find("input").focus();
		});
		$container.find("input").focus();
		$container.closest(".dropdown-panel").on("click", function() {
			$container.find("input").focus();
		});
		gitplex.revisionSelector.bindInputKeys(containerId, callback);
	},
	bindInputKeys: function(containerId, callback) {
		var $container = $("#" + containerId);
		var $input = $container.find("input");
		$input.bind("keydown", "return", function() {
			callback("return");
		});
		function scrollIfNecessary() {
			var margin = 8;
			var $refs = $container.find("ul.refs");
			var $active = $refs.find("li.active");
			var contentTop = $refs.offset().top;
			var activeTop = $active.offset().top;
			if (activeTop-margin<contentTop)
				$refs.scrollTop($refs.scrollTop()-(contentTop-activeTop+margin));
			var contentBottom = contentTop + $refs.height();
			var activeBottom = activeTop + $active.height();
			if (activeBottom+margin>contentBottom) 
				$refs.scrollTop($refs.scrollTop()+(activeBottom+margin-contentBottom));
		};
		$input.bind("keydown", "up", function(e) {
			e.preventDefault();
			var $active = $container.find("ul.refs li.active");
			var $prev = $active.prev("li");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
				callback("up");
			}
			scrollIfNecessary();
		});
		$input.bind("keydown", "down", function(e) {
			e.preventDefault();
			var $active = $container.find("ul.refs li.active");
			var $next = $active.next("li");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
				callback("down");
			}
			scrollIfNecessary();
		});
	}
};
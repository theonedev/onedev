gitplex.blobInstantSearch = {
	init: function(inputId, dropdownId, callback) {
		var $input = $("#" + inputId);
		var $dropdown = $("#" + dropdownId);
		$dropdown.on("hide", function() {
			if (!$input.is(":focus") && $input.is(":visible")) {
				// in case user clicks advanced button while the 
				// instant input is shown, we need to hide the 
				// input after a timeout in order not to make 
				// the advanced button moving to lost the click
				setTimeout(function(){$input.hide();}, 250);
			}
		});
		$input.on("blur", function() {
			if (!$dropdown.is(":visible") && $input.is(":visible")) {
				// in case user clicks advanced button while the 
				// instant input is shown, we need to hide the 
				// input after a timeout in order not to make 
				// the advanced button moving to lost the click
				setTimeout(function(){$input.hide();}, 250);
			}
		});
		$input.bind("keydown", "esc", function() {
			$input.hide();
		});
		$input.bind("keydown", "return", function() {
			callback("return");
		});
		$dropdown.bind("keydown", "return", function() {
			callback("return");
		});
		function scrollIfNecessary() {
			var margin = 36;
			var $container = $dropdown.find(".instant-search-result");
			var $active = $dropdown.find("li.hit.active");
			var containerTop = $container.offset().top;
			var activeTop = $active.offset().top;
			if (activeTop-margin<containerTop)
				$container.scrollTop($container.scrollTop()-(containerTop-activeTop+margin));
			var containerBottom = containerTop + $container.height();
			var activeBottom = activeTop + $active.height();
			
			if (activeBottom+margin>containerBottom) 
				$container.scrollTop($container.scrollTop()+(activeBottom+margin-containerBottom));
		};
		function keyup(e) {
			e.preventDefault();
			var $active = $dropdown.find("li.hit.active");
			var $prev = $active.prev("li.hit");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
				callback("up");
			} else if ($active.closest(".texts.section").length != 0) {
				var $lastSymbolHit = $dropdown.find(".symbols.section li.hit:last-child");
				if ($lastSymbolHit.length != 0) {
					$active.removeClass("active");
					$lastSymbolHit.addClass("active");
					callback("up");
				}
			}
			scrollIfNecessary();
		};
		function keydown(e) {
			e.preventDefault();
			var $active = $dropdown.find("li.hit.active");
			var $next = $active.next("li.hit");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
				callback("down");
			} else if ($active.closest(".symbols.section").length != 0) {
				var $firstTextHit = $dropdown.find(".texts.section li.hit:first-child");
				if ($firstTextHit.length != 0) {
					$active.removeClass("active");
					$firstTextHit.addClass("active");
					callback("down");
				}
			}
			scrollIfNecessary();
		}
		$input.bind("keydown", "up", function(e) {
			keyup(e);
		});
		$input.bind("keydown", "down", function(e) {
			keydown(e);
		});
		$dropdown.bind("keydown", "up", function(e) {
			keyup(e);
		});
		$dropdown.bind("keydown", "down", function(e) {
			keydown(e);
		});
	}
};
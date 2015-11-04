gitplex.blobInstantSearch = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		
		$input.on("blur", function() {
			if ($input.data("hint") == null && $input.is(":visible")) {
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

		$input.data("return", function() {
			if (pmease.commons.form.confirmLeave())
				callback("return");
		});
		$input.data("scrollIfNecessary", function($hint) {
			var margin = 36;
			var $container = $hint.find(".instant-search-result");
			var $active = $hint.find("li.hit.active");
			var containerTop = $container.offset().top;
			var activeTop = $active.offset().top;
			if (activeTop-margin<containerTop)
				$container.scrollTop($container.scrollTop()-(containerTop-activeTop+margin));
			var containerBottom = containerTop + $container.height();
			var activeBottom = activeTop + $active.height();
			
			if (activeBottom+margin>containerBottom) 
				$container.scrollTop($container.scrollTop()+(activeBottom+margin-containerBottom));
		});
		$input.data("keyup", function(e) {
			var $hint = $input.data("hint");
			if ($hint != null) {
				e.preventDefault();
				var $active = $hint.find("li.hit.active");
				var $prev = $active.prev("li.hit");
				if ($prev.length != 0) {
					$active.removeClass("active");
					$prev.addClass("active");
					callback("up");
				} else if ($active.closest(".texts.section").length != 0) {
					var $lastSymbolHit = $hint.find(".symbols.section li.hit:last-child");
					if ($lastSymbolHit.length != 0) {
						$active.removeClass("active");
						$lastSymbolHit.addClass("active");
						callback("up");
					}
				}
				$input.data("scrollIfNecessary")($hint);
			}
		});
		$input.data("keydown", function(e) {
			var $hint = $input.data("hint");
			if ($hint != null) {
				e.preventDefault();
				var $active = $hint.find("li.hit.active");
				var $next = $active.next("li.hit");
				if ($next.length != 0) {
					$active.removeClass("active");
					$next.addClass("active");
					callback("down");
				} else if ($active.closest(".symbols.section").length != 0) {
					var $firstTextHit = $hint.find(".texts.section li.hit:first-child");
					if ($firstTextHit.length != 0) {
						$active.removeClass("active");
						$firstTextHit.addClass("active");
						callback("down");
					}
				}
				$input.data("scrollIfNecessary")($hint);
			}
		});
		$input.bind("keydown", "return", $input.data("return"));
		$input.bind("keydown", "up", $input.data("keyup"));
		$input.bind("keydown", "down", $input.data("keydown"));
	},
	
	hintOpened: function(inputId, hintId) {
		var $input = $("#" + inputId);
		var $hint = $("#" + hintId);
		$hint.bind("keydown", "up", $input.data("keyup"));
		$hint.bind("keydown", "down", $input.data("keydown"));
		$hint.bind("keydown", "return", $input.data("return"));
		
		$hint.on("close", function() {
			if (!$input.is(":focus") && $input.is(":visible")) {
				// in case user clicks advanced button while the 
				// instant input is shown, we need to hide the 
				// input after a timeout in order not to make 
				// the advanced button moving to lost the click
				setTimeout(function(){$input.hide();}, 250);
			}
		});
		
		$hint.on("close", function() {
			$input.data("hint", null);
		});
		$input.data("hint", $hint);
	}
	
};
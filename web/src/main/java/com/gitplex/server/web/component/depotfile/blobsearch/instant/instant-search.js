gitplex.server.blobInstantSearch = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		
		$input.doneEvents("inputchange focus click", function() {
			callback("input", $(this).val());
		}, 100);
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
			if (gitplex.commons.form.confirmLeave()) {
				var $hint = $input.data("hint");
				if ($hint != null) {
					var $active = $hint.find("li.hit.active");
					if ($active.length != 0) {
						callback("return", $active.index());
					}
				}				
			}
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
				} 
				$hint.scrollIntoView("li.hit.active", 36, 36);
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
				} 
				$hint.scrollIntoView("li.hit.active", 36, 36);
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
			$input.data("hint", null);
		});
		
		$input.data("hint", $hint);
	}
	
};
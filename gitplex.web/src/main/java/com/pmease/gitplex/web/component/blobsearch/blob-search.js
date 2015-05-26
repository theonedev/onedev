gitplex.blobSearch = {
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
			var margin = 25;
			var $content = $dropdown.find(">.content");
			var $active = $dropdown.find("tr.hit.active");
			var contentTop = $content.offset().top;
			var activeTop = $active.offset().top;
			if (activeTop-margin<contentTop)
				$content.scrollTop($content.scrollTop()-(contentTop-activeTop+margin));
			var contentBottom = contentTop + $content.height();
			var activeBottom = activeTop + $active.height();
			if (activeBottom+margin>contentBottom) 
				$content.scrollTop($content.scrollTop()+(activeBottom+margin-contentBottom));
		};
		function keyup() {
			var $active = $dropdown.find("tr.hit.active");
			var $prev = $active.prev("tr.hit");
			if ($prev.length != 0) {
				$active.removeClass("active");
				$prev.addClass("active");
				callback("up");
			} else if ($active.closest(".texts.section").length != 0) {
				var $lastSymbolHit = $dropdown.find(".symbols.section tr.hit:last-child");
				if ($lastSymbolHit.length != 0) {
					$active.removeClass("active");
					$lastSymbolHit.addClass("active");
					callback("up");
				}
			}
			scrollIfNecessary();
		};
		function keydown() {
			var $active = $dropdown.find("tr.hit.active");
			var $next = $active.next("tr.hit");
			if ($next.length != 0) {
				$active.removeClass("active");
				$next.addClass("active");
				callback("down");
			} else if ($active.closest(".symbols.section").length != 0) {
				var $firstTextHit = $dropdown.find(".texts.section tr.hit:first-child");
				if ($firstTextHit.length != 0) {
					$active.removeClass("active");
					$firstTextHit.addClass("active");
					callback("down");
				}
			}
			scrollIfNecessary();
		}
		$input.bind("keydown", "up", function() {
			keyup();
		});
		$input.bind("keydown", "down", function() {
			keydown();
		});
		$dropdown.bind("keydown", "up", function() {
			keyup();
		});
		$dropdown.bind("keydown", "down", function() {
			keydown();
		});
	}
};
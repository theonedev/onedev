gitplex.blobSearcher = {
	init: function(inputId, dropdownId, callback) {
		var $input = $("#" + inputId);
		var $dropdown = $("#" + dropdownId);
		$input.bind("keydown", "return", function() {
			callback("return");
		});
		$dropdown.bind("keydown", "return", function() {
			callback("return");
		});
		function keyup() {
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
		};
		function keydown() {
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
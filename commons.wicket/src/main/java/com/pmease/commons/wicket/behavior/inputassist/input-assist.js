pmease.commons.inputassist = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		$input.data("callback", callback);
		$input.on("input", function() {
			callback($input.val(), $input.caret());
		});
		
		$input.data("update", function($item) {
			$input.val($item.data("input"));
			var cursor = $item.data("cursor");
			if (cursor != undefined)
				$input.cursor(cursor);
			$input.focus();
			$input.data("callback")(value, $input.cursor());
		});
		
		$input.on("keydown", "up", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				$active.removeClass("active");
				var $prev = $active.prev();
				if ($prev.length != 0)
					$prev.addClass("active");
				else
					$dropdown.find("li.selectable").last().addClass("active");
				return false;
			}
		});
		
		$input.on("keydown", "down", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				$input.data("update")($dropdown.find("li.active"));
				return false;
			}
		});
		
		$input.on("keydown", "return", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				$active.removeClass("active");
				var $next = $active.next();
				if ($next.length != 0)
					$next.addClass("active");
				else
					$dropdown.find("li.selectable").first().addClass("active");
				return false;
			}
		});
		
	},

	markErrors: function(inputId, inputErrors) {
		var $input = $("#" + inputId);
	},
	
	assistOpened: function(inputId, dropdownId) {
		var $input = $("#" + inputId);
		var $dropdown = $("#" + dropdownId);
		$input.data("dropdown", $dropdown);
		$dropdown.on("close", function() {
			$input.data("dropdown", null);
		});
		pmease.commons.inputassist.assistUpdated(inputId, dropdownId);
	},
	
	assistUpdated: function(inputId, dropdownId) {
		var $input = $("#" + inputId);
		var $dropdown = $("#" + dropdownId);
		$dropdown.click(function() {
			$input.focus();
		});
		var $item = $dropdown.find("li.selectable");
		$item.first().addClass("active");
		$item.click(function(){
			var $this = $(this);
			$input.data("update")($this);
		});
	}
}
pmease.commons.inputassist = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		$input.data("callback", callback);
		
		$input.doneevents("input focus mouseup keyup", function() {
			callback($input.val(), $input.caret());
		});
		
		$input.data("update", function($item) {
			var value = $item.data("input");
			$input.val(value);
			var cursor = $item.data("cursor");
			if (cursor != undefined)
				$input.caret(cursor);
			$input.focus();
			$input.data("callback")(value, $input.caret());
		});
		
		$input.bind("keydown", "up", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				$active.removeClass("active");
				var $prev = $active.prev();
				if ($prev.length != 0)
					$prev.addClass("active").focus();
				else
					$dropdown.find("li.selectable").last().addClass("active").focus();
				$input.focus();
				return false;
			}
		});
		
		$input.bind("keydown", "down", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				$active.removeClass("active");
				var $next = $active.next();
				if ($next.length != 0)
					$next.addClass("active").focus();
				else
					$dropdown.find("li.selectable").first().addClass("active").focus();
				$input.focus();
				return false;
			}
		});
		
		$input.bind("keydown", "return", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				$input.data("update")($dropdown.find("li.active"));
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
		$dropdown.data("trigger", $input);
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
		$item.click(function() {
			var $this = $(this);
			$input.data("update")($this);
		});
	}
}
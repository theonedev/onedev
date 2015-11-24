pmease.commons.inputassist = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		$input.data("callback", callback);
		
		$input.doneEvents("input mouseup keyup", function(e) {
			if (e.keyCode != 27 && e.keyCode != 38 && e.keyCode != 40 && e.keyCode != 13) // ignore esc, enter, up and down key 
				callback($input.val(), $input.caret());
		}, 100);
		
		$input.data("update", function($item) {
			var value = $item.data("content");
			$input.val(value);
			var caret = $item.data("caret");
			if (caret != undefined)
				$input.caret(caret);
			$input.focus();
			$input.data("callback")(value, $input.caret());
		});
		
		$input.bind("keydown", "up", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				if ($active.length != 0) {
					$active.removeClass("active");
					var $prev = $active.prev();
					if ($prev.length != 0)
						$prev.addClass("active").focus();
					else
						$dropdown.find("li.selectable").last().addClass("active").focus();
				} else {
					$dropdown.find("li.selectable").last().addClass("active").focus();
				}
				$input.focus();
				return false;
			}
		});
		
		$input.bind("keydown", "down", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				if ($active.length != 0) {
					$active.removeClass("active");
					var $next = $active.next();
					if ($next.length != 0)
						$next.addClass("active")[0].focus();
					else
						$dropdown.find("li.selectable").first().addClass("active").focus();
				} else {
					$dropdown.find("li.selectable").first().addClass("active").focus();
				}
				$input.focus();
				return false;
			}
		});
		
		$input.bind("keydown", "return", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				if ($active.length != 0) {
					$input.data("update")($active);
					return false;
				}
			}
		});
		
		callback($input.val());
	},

	markErrors: function(inputId, errors) {
		var $input = $("#" + inputId);
		$input.data("errors", errors);
		var $parent = $input.closest("form");
		$parent.css("position", "relative");
		$parent.find(">.input-error-mark").remove();
		if ($input.val().length != 0) {
			for (var i in errors) {
				var error = errors[i];
				var fromCoord = getCaretCoordinates($input[0], error.from);
				var toCoord = getCaretCoordinates($input[0], error.to);
				var $error = $("<div class='input-error-mark'></div>");
				$error.appendTo($parent);
				var inputCoord = $input.offset();
				var parentCoord = $parent.offset();
				var errorHeight = 5;
				var errorOffset = 9;
				var minWidth = 5;
				var left = fromCoord.left + inputCoord.left - parentCoord.left;
				var top = inputCoord.top + $input.outerHeight() - parentCoord.top - errorOffset;
				$error.css({left: left, top: top});
				$error.outerWidth(Math.max(toCoord.left-fromCoord.left, minWidth));
				$error.outerHeight(errorHeight);
			}
		}
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
		if (!$input.data("errors").length != 0)
			$item.first().addClass("active");
		$item.click(function() {
			var $this = $(this);
			$input.data("update")($this);
		});
	}
}
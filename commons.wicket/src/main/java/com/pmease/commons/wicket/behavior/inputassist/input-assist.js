pmease.commons.inputassist = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		$input.data("callback", callback);
		
		$input.data("prevValue", $input.val());
		$input.data("prevCaret", -1);
		$input.on("input click keyup", function(e) {
			var value = $input.val();
			var caret = $input.caret();
			if (value != $input.data("prevValue") || caret != $input.data("prevCaret")) {
				$input.data("prevValue", value);
				$input.data("prevCaret", caret);
				if (e.keyCode != 27 && e.keyCode != 13) // ignore esc, enter, up and down key 
					callback(value, caret, 1);
			}
		});
		$input.on("blur", function(e) {
			$input.data("prevCaret", -1);
		});

		$input.data("update", function($item) {
			var value = $item.data("content");
			$input.val(value);
			var caret = $item.data("caret");
			if (caret != undefined)
				$input.caret(caret);
			$input.focus();
			$input.data("callback")(value, $input.caret(), 1);
		});
		
		$input.bind("keydown", "up", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("tr.active");
				if ($active.length != 0) {
					$active.removeClass("active");
					var $prev = $active.prev();
					if ($prev.length != 0)
						$prev.addClass("active");
					else
						$dropdown.find("tr").last().addClass("active");
				} else {
					$dropdown.find("tr").last().addClass("active");
				}
				$dropdown.find(".suggestions").scrollIntoView("tr.active");
				pmease.commons.inputassist.updateHelp($dropdown);
				return false;
			}
		});
		
		$input.bind("keydown", "down", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("tr.active");
				if ($active.length != 0) {
					$active.removeClass("active");
					var $next = $active.next();
					if ($next.length != 0)
						$next.addClass("active");
					else
						$dropdown.find("tr").first().addClass("active");
				} else {
					$dropdown.find("tr").first().addClass("active");
				}
				$dropdown.find(".suggestions").scrollIntoView("tr.active");
				pmease.commons.inputassist.updateHelp($dropdown);
				return false;
			}
		});
		
		$input.bind("keydown", "return", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("tr.active");
				if ($active.length != 0) {
					$input.data("update")($active);
					return false;
				} else {
					pmease.commons.floating.close($dropdown, true);
				}
			}
		});
		
		$input.bind("keydown", "tab", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("tr.active");
				if ($active.length != 0) 
					$input.data("update")($active);
				else 
					$input.data("update")($dropdown.find("tr").first());
				return false;
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
				var toCoord = getCaretCoordinates($input[0], error.to+1);
				var $error = $("<div class='input-error-mark'></div>");
				$error.appendTo($parent);
				var inputCoord = $input.offset();
				var parentCoord = $parent.offset();
				var textHeight = 16;
				var errorHeight = 5;
				var errorOffset = 9;
				var minWidth = 5;
				var left = fromCoord.left + inputCoord.left - parentCoord.left;
				var top = fromCoord.top + inputCoord.top - parentCoord.top + textHeight; 
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
		var $item = $dropdown.find("tr");
		$item.click(function() {
			var $this = $(this);
			$input.data("update")($this);
		});
		pmease.commons.inputassist.updateHelp($dropdown);
	},
	
	initInfiniteScroll: function(assistId, callback) {
		$("#" + assistId + " .suggestions").scroll(function() {
			if($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight)
				callback();
		});
	},
	
	updateHelp: function($dropdown) {
		if ($dropdown.find("tr.active").length != 0) {
			$dropdown.find(".help").empty().append("Press 'enter' to complete selected item");
		} else {
			$dropdown.find(".help").empty().append("Press 'tab' to complete first item");
		}
	}
}
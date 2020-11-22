onedev.server.inputassist = {
	onDomReady: function(inputId, callback) {
		var $input = $("#" + inputId);
		
		onedev.server.inputassist.markErrors(inputId, []);
		
		$input.data("prevValue", $input.val());
		$input.data("prevCaret", -1);
		$input.on("paste click keyup assist", function(e) {
			var value = $input.val();
			var caret;
			if ($input.is(":focus"))
				caret = $input.caret();
			else
				caret = -1;
			if (value != $input.data("prevValue") || caret != $input.data("prevCaret") || !$input.data("dropdown")) {
				$input.data("prevValue", value);
				$input.data("prevCaret", caret);
				if ($input.is(":focus") && e.keyCode != 27 && e.keyCode != 13) // ignore esc, enter
					callback("input", value, caret);
			}
			if (value.trim().length == 0)
				onedev.server.inputassist.markErrors(inputId, []);
		});
		$input.on("clear", function(e) {
			onedev.server.inputassist.markErrors(inputId, []);
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
			$input.blur();
			$input.focus();
			$input.trigger("input");
			$input.trigger("assist");
		});
		
		$input.bind("keydown", "up", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				if ($active.length != 0) {
					var $prev = $active.prev("li:not(.loading-indicator)");
					if ($prev.length != 0) {
						$prev.addClass("active");
						$active.removeClass("active");
					}
				} else {
					$dropdown.find("li:not(.loading-indicator)").last().addClass("active");
				}
				$dropdown.find(".suggestions li.active").scrollIntoView();
				onedev.server.inputassist.updateHelp($dropdown);
				$dropdown.align($dropdown.data("alignment"));
				return false;
			}
		});
		
		$input.bind("keydown", "down", function() {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				if ($active.length != 0) {
					var $next = $active.next("li:not(.loading-indicator)");
					if ($next.length != 0) {
						$next.addClass("active");
						$active.removeClass("active");
					}
				} else {
					$dropdown.find("li:not(.loading-indicator)").first().addClass("active");
				}
				$dropdown.find(".suggestions li.active").scrollIntoView();
				onedev.server.inputassist.updateHelp($dropdown);
				$dropdown.align($dropdown.data("alignment"));
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
				} else {
					callback("close");
				}
			}
		});

		var tabbing = false;
		function tab() {
			tabbing = true;
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				if ($dropdown.data("inputContent") != $input.val()) {
					setTimeout(tab, 10);
				} else {
					tabbing = false;
					var $active = $dropdown.find("li.active");
					if ($active.length != 0) 
						$input.data("update")($active);
					else if ($dropdown.find("li").length != 0)
						$input.data("update")($dropdown.find("li").first());
				}
				return false;
			} else {
				tabbing = false;
			}
		};
		$input.bind("keydown", "tab", function() {
			if (!tabbing) {
				return tab();
			} else {
				return false;
			}
		});
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
				var minWidth = 5;
				var textMargin = 10;
				var left = fromCoord.left + inputCoord.left - parentCoord.left - $input.scrollLeft();
				if (left < $input.offset().left - $parent.offset().left + textMargin)
					left = $input.offset().left - $parent.offset().left + textMargin;
				var top = fromCoord.top + inputCoord.top - parentCoord.top + textHeight - $input.scrollTop(); 
				if (top < $input.offset().top - $parent.offset().top + textMargin)
					top = $input.offset().top - $parent.offset().top + textMargin;
				$error.css({left: left, top: top});
				$error.outerWidth(Math.max(toCoord.left-fromCoord.left, minWidth));
				$error.outerHeight(errorHeight);
			}
		}
	},
	
	assistOpened: function(inputId, dropdownId, inputContent) {
		var $input = $("#" + inputId);
		var $dropdown = $("#" + dropdownId);
		$dropdown.data("trigger", $input);
		$input.data("dropdown", $dropdown);
		$dropdown.on("close", function() {
			$input.data("dropdown", null);
		});
		onedev.server.inputassist.assistUpdated(inputId, dropdownId, inputContent);
	},
	
	assistUpdated: function(inputId, dropdownId, inputContent) {
		var $input = $("#" + inputId);
		var $dropdown = $("#" + dropdownId);
		$dropdown.data("inputContent", inputContent);
		$dropdown.click(function() {
			$input.focus();
		});
		var $item = $dropdown.find(".suggestion");
		$item.click(function() {
			var $this = $(this);
			$input.data("update")($this);
		});
		onedev.server.inputassist.updateHelp($dropdown);
		if ($input.is("textarea")) {
		    var e = $.Event('keydown');
		    e.which = 40; 
		    $input.trigger(e);			
		}
	},
	
	initInfiniteScroll: function(assistId, callback) {
		$("#" + assistId + " .suggestions").scroll(function() {
			if($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
				callback();
			}
		});
	},
	
	updateHelp: function($dropdown) {
		if ($dropdown.find("li.active").length != 0) {
			$dropdown.find(".help .complete").empty().append("Tab or &crarr; to use selected item.");
		} else {
			$dropdown.find(".help .complete").empty().append("Tab to use first item.");
		}
	}
};
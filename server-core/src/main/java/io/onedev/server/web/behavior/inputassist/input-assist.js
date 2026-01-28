onedev.server.inputassist = {
	onDomReady: function(inputId, callback, supportNaturalLanguageInput, translations) {
		var $input = $("#" + inputId);	
		$input.data("supportNaturalLanguageInput", supportNaturalLanguageInput);
		onedev.server.inputassist.translations = translations;
		
		onedev.server.inputassist.markErrors(inputId, []);
		
		$input.data("prevValue", $input.val());
		$input.data("prevCaret", -1);
		$input.on("keydown", function(e) {
			if (e.keyCode == 75 && (e.ctrlKey || e.metaKey) && !e.shiftKey) // command palette
				$input.blur();
		});
		$input.on("paste click keyup assist", function(e) {		
			// For click events, defer caret reading to allow browser to update position
			if (e.type === "click") {
				setTimeout(function() {
					$input.trigger("assist");
				}, 0);
				return;
			}
			
			var value = $input.val();
			var caret;
			var selectionStart, selectionEnd;
			if ($input.is(":focus")) {
				selectionStart = $input[0].selectionStart;
				selectionEnd = $input[0].selectionEnd;
				caret = selectionEnd;
			} else {
				caret = -1;
				selectionStart = -1;
				selectionEnd = -1;
			}
			
			// For suggestion logic, treat selections as if they are cleared
			if (selectionStart !== -1 && selectionStart !== selectionEnd) {
				// Remove the selected portion from value and set caret at selection start
				value = value.substring(0, selectionStart) + value.substring(selectionEnd);
				caret = selectionStart;
			}
			
			if (value != $input.data("prevValue") || caret != $input.data("prevCaret") || !$input.data("dropdown")) {
				$input.data("prevValue", value);
				$input.data("prevCaret", caret);
				if ($input.is(":focus") && e.keyCode != 27 && e.keyCode != 13) { // ignore esc, enter	
					if (caret != -1 && $input.data("supportNaturalLanguageInput")) {
						var contentBeforeCaret = value.substring(0, caret);
						if (contentBeforeCaret.endsWith("🤖")) {
							var contentToTranslate = contentBeforeCaret.substring(0, contentBeforeCaret.length-2);
							onedev.server.inputassist.showNaturalLanguageTranslatingIndicator($input, caret);						
							callback("translate", contentToTranslate, caret);
						} else {
							callback("input", value, caret, e.type);
						}
					} else {
						callback("input", value, caret, e.type);
					}
				}
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
		$input.on("focus", function(e) {
			if (!$input.data("programmaticFocus")) {
				$input.select();
			}
			$input.data("programmaticFocus", false);
		});

		$input.data("update", function($item) {
			var value = $item.data("content");
			var caret = $item.data("caret");
			if (value != $input.val() || caret != undefined && caret != $input.caret()) {
				$input.val(value);
				if (caret != undefined)
					$input.caret(caret);
				$input.blur();
				$input.data("programmaticFocus", true);
				$input.focus();
				$input.trigger("input");
				$input.trigger("assist");
				return true;
			} else {
				return false;
			}
		});
		
		function keyUpOrDown(keyUp) {
			var $dropdown = $input.data("dropdown");
			if ($dropdown) {
				var $active = $dropdown.find("li.active");
				if ($active.length != 0) {
					var $next;
					if (keyUp) 
						$next = $active.prev("li:not(.loading-indicator)");
					else
						$next = $active.next("li:not(.loading-indicator)");
					if ($next.length != 0) {
						$next.addClass("active");
						$active.removeClass("active");
					}
				} else {
					if (keyUp)
						$dropdown.find("li:not(.loading-indicator)").last().addClass("active");
					else
						$dropdown.find("li:not(.loading-indicator)").first().addClass("active");						
				}
				$dropdown.find(".suggestions li.active")[0].scrollIntoViewIfNeeded(false);
				onedev.server.inputassist.updateHelp($dropdown);
				$dropdown.align($dropdown.data("alignment"));
				return false;
			}
		}
		$input.bind("keydown", "up", function() {
			return keyUpOrDown(true);
		});
		$input.bind("keydown", "down", function() {
			return keyUpOrDown(false);
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
					var $active = $dropdown.find("li.suggestion.active");
					if ($active.length != 0) {
						$input.data("update")($active);
					} else if ($dropdown.find("li.suggestion").length != 0) {
						$dropdown.find("li.suggestion").each(function() {
							if ($input.data("update")($(this)))
								return false;
						});
					}
				}
				return false;
			} else {
				tabbing = false;
			}
		}
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
		var $form = $input.closest("form");
		$form.css("position", "relative");
		$form.find(">.input-error-mark").remove();
		
		// If there's a selection, errors were calculated based on effective value 
		// (with selection treated as deleted). Map error positions back to actual value.
		var selectionStart = $input[0].selectionStart;
		var selectionEnd = $input[0].selectionEnd;
		var selectionLength = selectionEnd - selectionStart;
		
		if ($input.val().length != 0) {
			for (var i in errors) {
				var error = errors[i];
				var errorFrom = error.from;
				var errorTo = error.to;
				
				// Map error positions from effective value back to actual value
				if (selectionLength > 0) {
					// Errors at or after selectionStart need to be shifted by selectionLength
					// because in the actual value, the selection still exists
					if (errorFrom >= selectionStart) {
						errorFrom += selectionLength;
					}
					if (errorTo >= selectionStart) {
						errorTo += selectionLength;
					}
				}
				
				var fromCoord = getCaretCoordinates($input[0], errorFrom);
				var toCoord = getCaretCoordinates($input[0], errorTo+1);
				var $error = $("<div class='input-error-mark'></div>");
				$error.appendTo($form);
				var inputCoord = $input.offset();
				var parentCoord = $form.offset();
				var textHeight = 16;
				var errorHeight = 5;
				var minWidth = 5;
				var textMargin = 10;
				var left = fromCoord.left + inputCoord.left - parentCoord.left - $input.scrollLeft();
				if (left < $input.offset().left - $form.offset().left + textMargin)
					left = $input.offset().left - $form.offset().left + textMargin;
				var top = fromCoord.top + inputCoord.top - parentCoord.top + textHeight - $input.scrollTop(); 
				if (top < $input.offset().top - $form.offset().top + textMargin)
					top = $input.offset().top - $form.offset().top + textMargin;
				$error.css({left: left, top: top});
				$error.outerWidth(Math.max(toCoord.left-fromCoord.left, minWidth));
				$error.outerHeight(errorHeight);
			}
		}
	},
	
	appendSpace: function(inputId) {
		var $input = $("#" + inputId);
		$input.val($input.val() + " ");
		$input.caret($input.val().length + 1);
		$input.blur();
		$input.data("programmaticFocus", true);
		$input.focus();
		$input.trigger("input");
		$input.trigger("assist");
	},

	naturalLanguageTranslated: function(inputId, translatedInput) {
		var $input = $("#" + inputId);		
		onedev.server.inputassist.hideNaturalLanguageTranslatingIndicator($input);
		var caret = $input.caret();
		var content = translatedInput + $input.val().substring(caret);
		$input.val(content);
		$input.caret(translatedInput.length);
		$input.blur();
		$input.data("programmaticFocus", true);
		$input.focus();
		$input.trigger("input");
		$input.trigger("assist");
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
			$input.data("programmaticFocus", true);
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
			$dropdown.find(".help .complete").empty().append(onedev.server.inputassist.translations["activeHelp"]);
		} else {
			$dropdown.find(".help .complete").empty().append(onedev.server.inputassist.translations["inactiveHelp"]);
		}
	},
	
	showNaturalLanguageTranslatingIndicator: function($input, caret) {
		$input.prop("readonly", true);
		var $form = $input.closest("form");
		$form.css("position", "relative");
		
		var coord = getCaretCoordinates($input[0], caret);
		var icon = onedev.server.isDarkMode()? "sparkle.gif": "sparkle-dark.gif";
		var $indicator = $("<div class='ajax-loading-indicator natural-language-translating-indicator'><img src='/~img/" + icon + "' width='16' height='16'></div>");
		$indicator.appendTo($form);
		
		var inputCoord = $input.offset();
		var parentCoord = $form.offset();
		var left = coord.left + inputCoord.left - parentCoord.left - $input.scrollLeft() + 5;
		var top = coord.top + inputCoord.top - parentCoord.top - $input.scrollTop() - 3;
		
		$indicator.css({
			position: "absolute",
			left: left + "px",
			top: top + "px",
			zIndex: 1000
		});
	},
	
	hideNaturalLanguageTranslatingIndicator: function($input) {
		$input.prop("readonly", false);
		$input.closest("form").children(".natural-language-translating-indicator").remove();
	}
};
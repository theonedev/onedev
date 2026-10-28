onedev.server.datePicker = {
	onDomReady: function(inputId, withTime, rangeMode) {
		// set locale if available
		var locale = (navigator.language || 'en').toLowerCase();
		var shortLocale = locale.substr(0, 2);
		if (flatpickr.l10ns[shortLocale]) {
			flatpickr.localize(flatpickr.l10ns[shortLocale]);
		}

		var $input = $("#" + inputId);
		var open = false;
		var calendar = flatpickr($input[0], {
			mode: rangeMode ? "range" : "single",
			dateFormat: withTime? 'Y-m-d H:i': 'Y-m-d', 
			enableTime: withTime,
			allowInput: true,
			onOpen: function() {
				open = true;
			},
			onClose: function(selectedDates, dateStr, instance) {
				open = false;				
				if(selectedDates.length == 1 && rangeMode) {
					instance.setDate([selectedDates[0],selectedDates[0]], true);
				}
			}			
		});
		$input[0].addEventListener("change", function(e) {
			if (open) {
				e.stopImmediatePropagation();
			}
		});		

		$input.add(calendar.calendarContainer).keydown(function(e) {
			if (e.keyCode == 27 && open) {
				// Consume Escape before closing so the enclosing overlay stays open.
				e.preventDefault();
				e.stopPropagation();
				$input[0].focus();
				calendar.close();
			}
		});

		$input.keydown(function(e) {
			// Postpone submitting the form to give it a chance to process dirty flags.
			setTimeout(function() {
				if (e.keyCode == 13) { // Enter
					var submitButton = $input.closest('form').find("input[type='submit'],button[type='submit']");
					if (submitButton.length > 0) {
						submitButton.trigger("click");
					} else {
						$input.trigger("change");
					}
					calendar.close();
				}
			}, 0);
		});		
	}
}

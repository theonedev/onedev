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

		$input.keydown(function(e) {
			/* 
			 * Perform actions in a timeout for two reasons:
			 * 1. When Return is pressed, postpone submitting the form to give form chance to process 
			 * dirty flags
			 * 2. When datepicker is opened and Esc is pressed, postpone closing the datepicker to let 
			 * enclosing dialog/dropdown knows that there is a datepicker opening and will not close 
			 * itself
			 */
			setTimeout(function() {
				if (e.keyCode == 13) { // Enter
					var submitButton = $input.closest('form').find("input[type='submit'],button[type='submit']");
					if (submitButton.length > 0) {
						submitButton.trigger("click");
					} else {
						$input.trigger("change");
					}
					calendar.close();
				} else if (e.keyCode == 27) { // Esc
					calendar.close();
				}
			}, 0);
		});		
	}
}

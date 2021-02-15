onedev.server.datePicker = {
	onDomReady: function(inputId) {
		// set locale if available
		var locale = (navigator.language || 'en').toLowerCase();
		var shortLocale = locale.substr(0, 2);
		if (flatpickr.l10ns[shortLocale]) {
			flatpickr.localize(flatpickr.l10ns[shortLocale]);
		}

		var $input = $("#" + inputId);
		var calendar = flatpickr($input[0], {
			dateFormat: 'Y-m-d', 
			allowInput: true
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
					$input.closest('form').find("input[type='submit'],button[type='submit']").trigger("click");
				} else if (e.keyCode == 27) { // Esc
					calendar.close();
				}
			}, 0);
		});		
	}
}

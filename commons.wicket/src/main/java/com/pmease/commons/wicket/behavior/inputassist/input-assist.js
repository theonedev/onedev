pmease.commons.inputassist = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		$input.data("callback", callback);
		$input.on("input", function() {
			callback($input.val(), $input.caret());
		});
	},

	update: function(inputId, value, cursor) {
		var $input = $("#" + inputId);
		$input.val(value);
		$input.cursor(cursor);
		$input.data("callback")(value, cursor);
	},
	
	markErrors: function(inputId, inputErrors) {
		var $input = $("#" + inputId);
	}
	
}
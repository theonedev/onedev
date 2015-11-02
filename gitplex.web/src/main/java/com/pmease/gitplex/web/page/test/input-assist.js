gitplex.inputassist = {
	init: function(inputId, callback) {
		var $input = $("#" + inputId);
		$input.data("callback", callback);
		$input.on("input", function() {
			callback($input.val(), $input.caret());
		});
	},

	change: function(inputId, value, cursor) {
		var $input = $("#" + inputId);
		$input.val(value);
		$input.cursor(cursor);
		$input.data("callback")(value, cursor);
	},
	
	fitWindow: function(inputId, dropdownId) {
		var $input = $("#" + inputId);
		var $dropdown = $("#" + dropdownId);
		var dropdownSpace = $(window).height() - $input.offset().top - $input.height() - 45;
		$dropdown.find(".-assist").css("max-height", dropdownSpace);
	}
	
}
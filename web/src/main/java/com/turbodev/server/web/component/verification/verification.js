turbodev.server.verificationChoiceFormatter = {
	formatSelection: function(verification) {
		return verification.name;
	},
	
	formatResult: function(verification) {
		return "<div class='verification'>" + verification.name + "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

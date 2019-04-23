onedev.server.buildChoiceFormatter = {
	formatSelection: function(build) {
		return "#" + build.number;
	},
	
	formatResult: function(build) {
		return "<div class='build'>#" + build.number + "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

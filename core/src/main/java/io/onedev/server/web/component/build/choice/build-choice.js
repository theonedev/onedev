onedev.server.buildChoiceFormatter = {
	formatSelection: function(build) {
		return build.configuration + ":" + build.name;
	},
	
	formatResult: function(build) {
		return "<div class='build'>" + build.configuration + ":" + build.name + "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

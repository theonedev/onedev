onedev.server.buildChoiceFormatter = {
	formatSelection: function(build) {
		return "#" + build.number + " [" + build.configuration + "] " + build.commitShortMessage;
	},
	
	formatResult: function(build) {
		return "<div class='build'>#" + build.number + " [" + build.configuration + "] " + build.commitShortMessage + "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

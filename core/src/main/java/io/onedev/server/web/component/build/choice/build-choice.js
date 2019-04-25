onedev.server.buildChoiceFormatter = {
	formatSelection: function(build) {
		return "#" + build.number;
	},
	
	formatResult: function(build) {
		return "<div class='build'>#" + build.number + " (" + build.jobName + " : " + build.version + ")</div>";
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

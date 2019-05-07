onedev.server.buildChoiceFormatter = {
	formatSelection: function(build) {
		return "#" + build.number;
	},
	
	formatResult: function(build) {
        if (build.version)
            return "<div class='build'>#" + build.number + " (" + build.version + ") : " + build.jobName + "</div>";
        else
            return "<div class='build'>#" + build.number + " (" + build.version + ")</div>";
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

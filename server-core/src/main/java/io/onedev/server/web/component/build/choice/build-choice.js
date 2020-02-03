onedev.server.buildChoiceFormatter = {
	formatBuild: function(build) {
        if (build.version)
            return "<div class='build'>#" + build.number + " (" + build.version + ") : " + build.jobName + "</div>";
        else
            return "<div class='build'>#" + build.number + " : " + build.jobName + "</div>";
	},
	formatSelection: function(build) {
		return onedev.server.buildChoiceFormatter.formatBuild(build);
	},
	formatResult: function(build) {
		return onedev.server.buildChoiceFormatter.formatBuild(build);
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

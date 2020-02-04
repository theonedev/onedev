onedev.server.buildChoiceFormatter = {
	formatBuild: function(build) {
        if (build.version)
            return "<span class='build'>#" + build.number + " (" + build.version + ") : " + build.jobName + "</span>";
        else
            return "<span class='build'>#" + build.number + " : " + build.jobName + "</span>";
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

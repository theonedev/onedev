onedev.server.buildChoiceFormatter = {
	formatBuild: function(build) {
        if (build.version)
            return build.jobName + ": " + build.version + " (" + build.reference + ")";
        else
            return build.jobName + " (" + build.reference + ")";
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

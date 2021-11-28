onedev.server.buildChoiceFormatter = {
	formatBuild: function(build) {
		var buildNumber = "#" + build.number;
		if (build.project)
			buildNumber = build.project + buildNumber;
        if (build.version)
            return buildNumber + " (" + build.version + ") : " + build.jobName;
        else
            return buildNumber + " : " + build.jobName;
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

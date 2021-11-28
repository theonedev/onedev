onedev.server.pullRequestChoiceFormatter = {
	formatPullRequest: function(request) {
		if (request.project)
			return request.project + "#" + request.number + " " + request.title;
		else
			return "#" + request.number + " " + request.title;
	},
	formatSelection: function(request) {
		return onedev.server.pullRequestChoiceFormatter.formatPullRequest(request);
	},
	formatResult: function(request) {
		return onedev.server.pullRequestChoiceFormatter.formatPullRequest(request);
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

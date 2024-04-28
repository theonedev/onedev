onedev.server.pullRequestChoiceFormatter = {
	formatPullRequest: function(request) {
		return request.title + " (" + request.reference +")";
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

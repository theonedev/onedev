onedev.server.issueChoiceFormatter = {
	formatIssue: function(issue) {
		return issue.title + " (" + issue.reference +")";
	},
	formatSelection: function(issue) {
		return onedev.server.issueChoiceFormatter.formatIssue(issue);
	},
	formatResult: function(issue) {
		return onedev.server.issueChoiceFormatter.formatIssue(issue);
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

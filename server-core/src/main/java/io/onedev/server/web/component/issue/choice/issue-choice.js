onedev.server.issueChoiceFormatter = {
	formatIssue: function(issue) {
		if (issue.project)
			return issue.project + "#" + issue.number + " " + issue.title;
		else
			return "#" + issue.number + " " + issue.title;
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

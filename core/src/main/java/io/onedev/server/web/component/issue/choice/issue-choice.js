onedev.server.issueChoiceFormatter = {
	formatSelection: function(issue) {
		return "#" + issue.number + " " + issue.title;
	},
	
	formatResult: function(issue) {
		return "<div class='issue'>#" + issue.number + " " + issue.title + "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

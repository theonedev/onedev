onedev.server.pullRequestChoiceFormatter = {
	formatSelection: function(request) {
		return "#" + request.number + " " + request.title;
	},
	
	formatResult: function(request) {
		return "<div class='pull-request'>#" + request.number + " " + request.title + "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

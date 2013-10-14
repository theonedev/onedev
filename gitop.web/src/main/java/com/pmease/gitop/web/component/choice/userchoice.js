var UserChoice = UserChoice || {};
UserChoice.formatter = {
	formatSelection: function(user) {
		return "<img class='img-thumbnail avatar' src='" + user.avatar + "' /> " + user.name + " (" + user.displayName + ")";
	},
	
	formatResult: function(user) {
		return "<div class='user-choice-row'><img class='img-thumbnail avatar avatar-big' src='" + user.avatar + "' />" 
				+ "<p>"+ user.name + " (" + user.displayName + ")" + "</p>"
				+ "<p class='text-muted'>" + user.email + "</p>"
				+ "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};

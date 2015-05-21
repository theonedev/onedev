gitplex.userChoiceFormatter = {
	formatSelection: function(user) {
		return "<img class='avatar' src='" + user.avatar + "'/>" + user.name + (user.fullName?" (" + user.fullName + ")": "");
	},
	
	formatResult: function(user) {
		return "<div class='user'>" +
				"<img class='avatar' src='" + user.avatar + "'/>" +
				"<div class='name'>"+ user.name + (user.fullName?" (" + user.fullName + ")": "") + "</div>" +
				"<div class='email'>" + user.email + "</div>" +
				"</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

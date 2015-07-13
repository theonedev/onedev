gitplex.userChoiceFormatter = {
	formatSelection: function(user) {
		return "<img class='avatar' src='" + user.avatar + "'/>" + user.name + (user.fullName?" (" + user.fullName + ")": "");
	},
	
	formatResult: function(user) {
		if (!user.alias) {
			return "<div class='user'>" +
					"<img class='avatar' src='" + user.avatar + "'/>" +
					"<div class='name'>"+ user.name + (user.fullName?" (" + user.fullName + ")": "") + "</div>" +
					"<div class='email'>" + user.email + "</div>" +
					"</div>";
		} else {
			return "<i>&lt;&lt;" + user.alias + "&gt;&gt;</i>";
		}
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

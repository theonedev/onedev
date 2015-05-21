gitplex.account = {
	choiceFormatter: {
		formatSelection: function(user) {
			return user.fullName? user.fullName: user.name;
		},
		
		formatResult: function(user) {
			return "<div class='account'>" +
					"<img class='avatar' src='" + user.avatar + "'/>" +
					"<span class='name'>" + (user.fullName? user.fullName: user.name) + "</span>";
		},
		
		escapeMarkup: function(m) {
			return m;
		}
	}
		
}

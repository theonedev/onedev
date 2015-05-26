gitplex.account = {
	choiceFormatter: {
		formatSelection: function(user) {
			return user.name;
		},
		
		formatResult: function(user) {
			return "<div class='account'>" +
					"<img class='avatar' src='" + user.avatar + "'></img>" +
					"<span class='name'>" + user.name + "</span>" +
					"</div>";
		},
		
		escapeMarkup: function(m) {
			return m;
		}
	}
		
}

gitplex.server.accountChoiceFormatter = {
	formatSelection: function(account) {
		return "<img class='avatar' src='" + account.avatar + "'/>" + account.name;
	},
	
	formatResult: function(account) {
		if (!account.alias) {
			return "<div class='account'>" +
					"<img class='avatar' src='" + account.avatar + "'/>" +
					"<span class='name'>"+ account.name + "</span>" +
					"</div>";
		} else {
			return "<i>&lt;&lt;" + account.alias + "&gt;&gt;</i>";
		}
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

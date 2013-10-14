var TeamChoice = TeamChoice || {};
TeamChoice.formatter = {
	formatSelection: function(team) {
		return team.name;
	},
	
	formatResult: function(team) {
		return team.name;
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};

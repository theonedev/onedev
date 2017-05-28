gitplex.server.projectChoiceFormatter = {
	formatSelection: function(project) {
		return project.name;
	},
	
	formatResult: function(project) {
		return project.name;
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};
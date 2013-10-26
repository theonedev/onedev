var ProjectChoice = ProjectChoice || {};
ProjectChoice.formatter = {
	formatSelection: function(project) {
		return project.name
	},
	
	formatResult: function(project) {
		return project.owner + '/' + project.name
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};

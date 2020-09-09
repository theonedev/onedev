onedev.server.projectChoiceFormatter = {
	formatSelection: function(project) {
		return "<img class='avatar' src='" + project.avatar + "'/><span class='name'>" + project.name + "</span>";
	},
	
	formatResult: function(project) {
		return "<div class='project'>" +
				"<img class='avatar' src='" + project.avatar + "'/>" +
				"<span class='name'>"+ project.name + "</span>" +
				"</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};
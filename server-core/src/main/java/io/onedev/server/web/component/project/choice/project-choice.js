onedev.server.projectChoiceFormatter = {
	formatSelection: function(project) {
		return "<img class='avatar' src='" + project.avatar + "'/><span class='path name'>" + project.path + "</span>";
	},
	
	formatResult: function(project) {
		return "<div class='project'>" +
				"<img class='avatar' src='" + project.avatar + "'/>" +
				"<span class='name path'>"+ project.path + "</span>" +
				"</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};
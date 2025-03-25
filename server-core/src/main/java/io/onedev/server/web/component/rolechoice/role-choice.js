onedev.server.roleChoiceFormatter = {
	formatSelection: function(role) {
		return role.name;
	},
	
	formatResult: function(role) {
		return role.name + (role.description ? " <span class='text-muted font-size-sm ml-1'>" + role.description + "</span>" : "");
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};
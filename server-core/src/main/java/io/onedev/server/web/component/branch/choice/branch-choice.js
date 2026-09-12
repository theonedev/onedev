onedev.server.branchChoiceFormatter = {
	formatSelection: function(branch) {
		return branch.name.escapeHtml();
	},
	
	formatResult: function(branch) {
		return branch.name.escapeHtml();
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};

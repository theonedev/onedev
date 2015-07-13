gitplex.repoChoiceFormatter = {
	formatSelection: function(repository) {
		return repository.name;
	},
	
	formatResult: function(repository) {
		return repository.name;
	},
	
	escapeMarkup: function(m) {
		return m;
	}
};
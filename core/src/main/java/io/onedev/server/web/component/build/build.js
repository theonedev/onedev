onedev.server.configurationChoiceFormatter = {
	formatSelection: function(configuration) {
		return configuration.name;
	},
	
	formatResult: function(configuration) {
		return "<div class='configuration'>" + configuration.name + "</div>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

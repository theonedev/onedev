onedev.server.iterationChoiceFormatter = {
	formatSelection: function(iteration) {
		return iteration.name;
	},
	
	formatResult: function(iteration) {
		return "<span class='iteration'>" + iteration.name + " <span class='ml-2 badge " + iteration.statusClass + "'>" + iteration.statusName + "</span></span>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

onedev.server.iterationChoiceFormatter = {
	formatSelection: function(iteration) {
		return iteration.name;
	},
	
	formatResult: function(iteration) {
		return "<span class='iteration d-flex justify-content-between'><span>" + iteration.name + "</span><span class='badge badge-sm " + iteration.statusClass + "'>" + iteration.statusName + "</span></span>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

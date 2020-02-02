onedev.server.milestoneChoiceFormatter = {
	formatSelection: function(milestone) {
		return milestone.name;
	},
	
	formatResult: function(milestone) {
		return "<span class='milestone'>" + milestone.name + " <span class='label " + milestone.statusClass + "'>" + milestone.statusName + "</span></span>";
	},
	
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

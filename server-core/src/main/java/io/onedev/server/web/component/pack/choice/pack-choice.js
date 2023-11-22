onedev.server.packChoiceFormatter = {
	formatPack: function(pack) {
		return pack.version;
	},
	formatSelection: function(pack) {
		return onedev.server.packChoiceFormatter.formatPack(pack);
	},
	formatResult: function(pack) {
		return onedev.server.packChoiceFormatter.formatPack(pack);
	},
	escapeMarkup: function(m) {
		return m;
	},
	
}; 

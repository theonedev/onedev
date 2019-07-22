onedev.server.plainDiff = {
	onDomReady: function(containerId, callback) {
		$("#" + containerId).data("callback", callback);
	},
	expand: function(containerId, blockIndex, expandedHtml) {
		var $container = $("#" + containerId);
		var $expanderTr = $container.find(".expander" + blockIndex);
		$expanderTr.replaceWith(expandedHtml); 
	}
};

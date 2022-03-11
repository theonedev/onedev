onedev.server.onQuickSearchDomReady = function(containerId, callback) {
	var $body = $("#" + containerId + ">.quick-search>.modal-body");
	
	$body.children("input").doneEvents("inputchange", function() {
		callback("input", $(this).val());
	}, 100).selectByTyping($body);
	
};
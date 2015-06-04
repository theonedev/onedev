gitplex.blobView = function(containerId) {
	var $blobView = $("#" + containerId + ">.blob-view");
	$blobView.on("autofit", function(event, width, height) {
		event.stopPropagation();
		$blobView.outerWidth(width);
		$blobView.outerHeight(height);
		var $head = $blobView.find(">.head");
		var $body = $blobView.find(">.body");
		
		height = $blobView.height()-$head.outerHeight();
		$body.outerWidth(width).outerHeight(height);
		$body.closestDescendant(".autofit").trigger("autofit", [$body.width(), $body.height()]);
	});
}
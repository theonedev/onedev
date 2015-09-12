gitplex.blobView = function(containerId) {
	var $blobView = $("#" + containerId + ">.blob-view");
	var $head = $blobView.find(">.head");
	var $body = $blobView.find(">.body");
	$body.scroll(function() {
    	pmease.commons.history.setScroll({left: $body.scrollLeft(), top: $body.scrollTop()});
	});

	$blobView.on("autofit", function(event, width, height) {
		event.stopPropagation();
		$blobView.outerWidth(width);
		$blobView.outerHeight(height);
		
		height = $blobView.height()-$head.outerHeight();
		$body.outerWidth(width).outerHeight(height);
		$body.closestDescendant(".autofit:visible").trigger("autofit", [$body.width(), $body.height()]);

		var scroll = pmease.commons.history.getScroll();
		if (scroll) {
		    $body.scrollLeft(scroll.left);
		    $body.scrollTop(scroll.top);
		}
	});
}
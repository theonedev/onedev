gitplex.blobView = function(containerId) {
	var $blobView = $("#" + containerId + ">.blob-view");
	var $head = $blobView.find(">.head");
	var $body = $blobView.find(">.body");
	$body.scroll(function() {
    	pmease.commons.history.setScrollPos({left: $body.scrollLeft(), top: $body.scrollTop()});
	});

	gitplex.spaceGreedy.getScrollTop = function() {
		return $body.scrollTop();
	};

	$blobView.on("autofit", function(event, width, height) {
		event.stopPropagation();
		$blobView.outerWidth(width);
		$blobView.outerHeight(height);
		
		height = $blobView.height()-$head.outerHeight();
		$body.outerWidth(width).outerHeight(height);
		$body.closestDescendant(".autofit").trigger("autofit", [$body.width(), $body.height()]);

		var scrollPos = pmease.commons.history.getScrollPos();
		if (scrollPos) {
		    $body.scrollLeft(scrollPos.left);
		    $body.scrollTop(scrollPos.top);
		}
	});
}
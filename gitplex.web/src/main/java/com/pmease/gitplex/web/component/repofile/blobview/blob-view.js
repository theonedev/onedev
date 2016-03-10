gitplex.blobView = function(containerId) {
	var $blobView = $("#" + containerId + ">.blob-view");
	var $content = $blobView.find(">.content");
	$content.scroll(function() {
    	pmease.commons.history.setScroll({left: $content.scrollLeft(), top: $content.scrollTop()});
	});

	$blobView.on("autofit", function(event, width, height) {
		event.stopPropagation();
		$blobView.outerWidth(width);
		$blobView.outerHeight(height);
		
		height = $blobView.height();
		$content.outerWidth(width).outerHeight(height);
		$content.closestDescendant(".autofit:visible").trigger("autofit", [$content.width(), $content.height()]);

		var scroll = pmease.commons.history.getScroll();
		if (scroll) {
		    $content.scrollLeft(scroll.left);
		    $content.scrollTop(scroll.top);
		}
	});
}
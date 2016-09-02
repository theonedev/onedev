gitplex.blobView = function(containerId) {
	var $blobView = $("#" + containerId + ">.blob-view");
	var $content = $blobView.find(">.content");

	$blobView.on("storeViewState", function(e) {
		e.stopPropagation();
		pmease.commons.history.setViewState({scroll:{left: $content.scrollLeft(), top: $content.scrollTop()}});
		$content.find(".autofit:visible").first().trigger("storeViewState");
	});
	
	$blobView.on("autofit", function(e, width, height) {
		e.stopPropagation();
		$blobView.outerWidth(width);
		$blobView.outerHeight(height);
		
		height = $blobView.height();
		$content.outerWidth(width).outerHeight(height);
		$content.find(".autofit:visible").first().trigger("autofit", [$content.width(), $content.height()]);

		var viewState = pmease.commons.history.getViewState();
		if (viewState && viewState.scroll) {
		    $content.scrollLeft(viewState.scroll.left);
		    $content.scrollTop(viewState.scroll.top);
		}
	});
};
gitplex.blobView = function(containerId) {
	var $blobView = $("#" + containerId + ">.blob-view");
	var $head = $blobView.find(">.head");
	var $body = $blobView.find(">.body");

	$blobView.on("storeViewState", function(e) {
		e.stopPropagation();
		pmease.commons.history.setViewState({scroll:{left: $body.scrollLeft(), top: $body.scrollTop()}});
		$body.find(".autofit:visible").first().trigger("storeViewState");
	});
	
	$blobView.on("autofit", function(e, width, height) {
		e.stopPropagation();
		$blobView.outerWidth(width);
		$blobView.outerHeight(height);
		
		console.log($head.outerHeight());
		height = $blobView.height()-$head.outerHeight();
		$body.outerWidth(width).outerHeight(height);
		$body.find(".autofit:visible").first().trigger("autofit", [$body.width(), $body.height()]);

		var viewState = pmease.commons.history.getViewState();
		if (viewState && viewState.scroll) {
		    $body.scrollLeft(viewState.scroll.left);
		    $body.scrollTop(viewState.scroll.top);
		}
	});
};
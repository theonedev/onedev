gitplex.server.blobView = function(containerId) {
	var $blobView = $("#" + containerId + ">.blob-view");
	var $head = $blobView.find(">.head");
	var $body = $blobView.find(">.body");

	$blobView.on("storeViewState", function(e) {
		e.stopPropagation();
		gitplex.commons.history.setViewState({scroll:{left: $body.scrollLeft(), top: $body.scrollTop()}});
		$body.find(".autofit:visible").first().trigger("storeViewState");
	});
	
	$blobView.on("autofit", function(e, width, height) {
		e.stopPropagation();
		$blobView.outerWidth(width);
		$blobView.outerHeight(height);
		
		height = $blobView.height()-$head.outerHeight();
		$body.outerWidth(width).outerHeight(height);
		$body.find(".autofit:visible").first().trigger("autofit", [$body.width(), $body.height()]);

		var viewState = gitplex.commons.history.getViewState();
		if (viewState && viewState.scroll) {
		    $body.scrollLeft(viewState.scroll.left);
		    $body.scrollTop(viewState.scroll.top);
		}
		if (location.hash) { // scroll markdown headlines into view
			var element = document.getElementsByName(decodeURIComponent(location.hash.slice(1)))[0];
			if (element)
				element.scrollIntoView();
		}
	});
};
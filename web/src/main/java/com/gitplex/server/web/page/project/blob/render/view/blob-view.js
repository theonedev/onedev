gitplex.server.blobView = {

	onDomReady: function(containerId) {
		var $blobView = $("#" + containerId + ">.blob-view");
		var $head = $blobView.find(">.head");
		var $body = $blobView.find(">.body");

	    if ($body.find(".autofit").length != 0)
	    	$body.css("overflow", "visible");
	    
		$blobView.on("getViewState", function(e) {
			return {scroll:{left: $body.scrollLeft(), top: $body.scrollTop()}};			
		});
		
		$blobView.on("setViewState", function(e, viewState) {
			if (viewState.scroll) {
				$body.scrollLeft(viewState.scroll.left);
				$body.scrollTop(viewState.scroll.top);
			}
		});
		
		$blobView.on("autofit", function(e, width, height) {
			$blobView.outerWidth(width);
			$blobView.outerHeight(height);
			
			height = $blobView.height()-$head.outerHeight();
			$body.outerWidth(width).outerHeight(height);
			$body.find(".autofit:visible").first().triggerHandler("autofit", [$body.width(), $body.height()]);
		});
	} 	

};

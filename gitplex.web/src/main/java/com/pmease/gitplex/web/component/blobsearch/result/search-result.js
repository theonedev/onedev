gitplex.blobSearchResult = {
	scrollIfNecessary: function(containerId) {
		var margin = 25;
		var $body = $("#" + containerId + ">.search-result>.body");
		
		var $active = $body.find("a.selectable.active");
		var contentTop = $body.offset().top;
		var activeTop = $active.offset().top;
		if (activeTop-margin<contentTop)
			$body.scrollTop($body.scrollTop()-(contentTop-activeTop+margin));
		var contentBottom = contentTop + $body.height();
		var activeBottom = activeTop + $active.height();
		if (activeBottom+margin>contentBottom) 
			$body.scrollTop($body.scrollTop()+(activeBottom+margin-contentBottom));
	}
}

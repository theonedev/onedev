gitplex.searchresult = {
	confirmSwitchFileByLink: function(linkId) {
		var linkURI = new URI(document.getElementById(linkId));
		var currentURI = new URI(window.location.href);
		if (linkURI.search(true).path != currentURI.search(true).path)
			return pmease.commons.form.confirmLeave();
		else
			return true;
	},
	confirmSwitchFileByPath: function(path) {
		var currentURI = new URI(window.location.href);
		if (path != currentURI.search(true).path)
			return pmease.commons.form.confirmLeave();
		else
			return true;
	},
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

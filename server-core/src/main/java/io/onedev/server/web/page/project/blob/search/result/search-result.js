onedev.server.searchResult = {
	confirmSwitchFileByLink: function(linkId) {
		var linkURI = new URI(document.getElementById(linkId));
		var currentURI = new URI(window.location.href);
		if (linkURI.path() != currentURI.path() || linkURI.hasQuery("mark") != currentURI.hasQuery("mark"))
			return onedev.server.form.confirmLeave();
		else
			return true;
	},
	confirmSwitchFileByPath: function(path, hasMark) {
		var currentURI = new URI(window.location.href);
		if (path != currentURI.path() || hasMark != currentURI.hasQuery("mark"))
			return onedev.server.form.confirmLeave();
		else
			return true;
	}
};

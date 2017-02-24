gitplex.server.searchResult = {
	confirmSwitchFileByLink: function(linkId) {
		var linkURI = new URI(document.getElementById(linkId));
		var currentURI = new URI(window.location.href);
		if (linkURI.path() != currentURI.path())
			return gitplex.server.form.confirmLeave();
		else
			return true;
	},
	confirmSwitchFileByPath: function(path) {
		var currentURI = new URI(window.location.href);
		if (path != currentURI.path())
			return gitplex.server.form.confirmLeave();
		else
			return true;
	}
};

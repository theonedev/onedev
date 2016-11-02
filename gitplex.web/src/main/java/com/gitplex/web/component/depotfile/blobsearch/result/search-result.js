gitplex.searchresult = {
	confirmSwitchFileByLink: function(linkId) {
		var linkURI = new URI(document.getElementById(linkId));
		var currentURI = new URI(window.location.href);
		if (linkURI.search(true).path != currentURI.search(true).path)
			return gitplex.commons.form.confirmLeave();
		else
			return true;
	},
	confirmSwitchFileByPath: function(path) {
		var currentURI = new URI(window.location.href);
		if (path != currentURI.search(true).path)
			return gitplex.commons.form.confirmLeave();
		else
			return true;
	}
};

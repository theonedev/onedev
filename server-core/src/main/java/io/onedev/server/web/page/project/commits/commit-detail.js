onedev.server.commitDetail = {
	initRefs: function(refsId) {
		var $refs = $("#" + refsId);
		if ($refs.children().length != 0)
			$refs.oneline("<a class='ellipsis-expander'><svg class='icon'><use xlink:href='" + onedev.server.icons + "#ellipsis'/></svg></a>", 10, 60);
		else 
			$refs.parent().remove();
	}
};
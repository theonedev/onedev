gitplex.commitdetail = {
	initRefs: function(refsId) {
		var $refs = $("#" + refsId);
		if ($refs.children().length != 0)
			$refs.oneline("<a class='ellipsis-expander'><i class='fa fa-ellipsis-h'></i></a>", 10, 100);
		else 
			$refs.parent().remove();
	}
}
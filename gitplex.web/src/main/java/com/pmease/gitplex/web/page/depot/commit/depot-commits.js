gitplex.repocommits = {
	initQuery: function() {
		var $queryForm = $("#repo-commits>.head>.query");
		var $queryInput = $queryForm.find(".input-group>input");
		$queryInput.clearable();
		$queryInput.on("input", function() {
			if (!$queryInput.is(":focus"))
				$queryForm.find("button[type='submit']").click();
		});
	}
}
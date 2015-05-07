gitplex.treelist = {
	renderLastCommits: function(containerId, lastCommits) {
		var $table = $("#" + containerId).find("table.tree-list");
		$table.find("tr.child").each(function() {
			var $row = $(this);
			var path = $row.find("td.path span").text();
			var index = path.indexOf('/');
			if (index != -1)
				path = path.substring(0, index);
			var lastCommit = lastCommits[path];
			
			var $summary = $row.children(".last-commit.summary");
			$summary.append("<a href='" + lastCommit.url + "'>" + lastCommit.summary + "</a>");
			$row.children(".last-commit.age").append(lastCommit.age);
		});
	}
}
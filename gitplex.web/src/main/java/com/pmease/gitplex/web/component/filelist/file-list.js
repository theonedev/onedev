gitplex.filelist = {
	init: function(containerId) {
		var $container = $("#" + containerId);
		var $fileList = $container.find(">.file-list");
		var $body = $fileList.find(">.body");
		
		gitplex.spaceGreedy.getScrollTop = function() {
			return $body.scrollTop();
		};
		
		$fileList.on("autofit", function(event, width, height) {
			event.stopPropagation();
			$fileList.outerWidth(width);
			$fileList.outerHeight(height);
			$body.outerWidth($fileList.width()).outerHeight($fileList.height()-$fileList.find(">.head").outerHeight());
		});
	},
	renderLastCommits: function(containerId, lastCommits) {
		var $table = $("#" + containerId).find(".file-list>.body>table");
		$table.find("tr.child").each(function() {
			var $row = $(this);
			var path = $row.find("td.path span").text();
			var index = path.indexOf('/');
			if (index != -1)
				path = path.substring(0, index);
			var lastCommit = lastCommits[path];
			
			var $summary = $row.children(".last-commit.summary");
			$summary.empty().append("<a href='" + lastCommit.url + "'>" + lastCommit.summary + "</a>");
			$row.children(".last-commit.age").append(lastCommit.age);
		});
	}
}
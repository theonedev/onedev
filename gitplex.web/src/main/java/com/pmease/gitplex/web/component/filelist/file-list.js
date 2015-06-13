gitplex.filelist = {
	init: function(containerId, lastCommitsUrl) {
		var $container = $("#" + containerId);
		var $fileList = $container.find(">.file-list");
		
		gitplex.spaceGreedy.getScrollTop = function() {
			return $fileList.scrollTop();
		};
		
		$fileList.on("autofit", function(event, width, height) {
			event.stopPropagation();
			$fileList.outerWidth(width);
			$fileList.outerHeight(height);
		});

		$.ajax({
			url: lastCommitsUrl,
			cache: false, 
			success: function(lastCommits) {
				if (document.contains($container[0])) { // add this check to avoid rendering last commits if directory is changed
					var $table = $container.find(".file-list>table");
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
		});
	}
}
gitplex.filelist = {
	init: function(containerId, lastCommitsUrl) {
		var $container = $("#" + containerId);
		var $fileList = $container.find(">.file-list");
		
		gitplex.expandable.getScrollTop = function() {
			return $fileList.scrollTop();
		};
		gitplex.expandable.setScrollTop = function(scrollTop) {
			$fileList.scrollTop(scrollTop);
		};
		
		$fileList.on("autofit", function(event, width, height) {
			event.stopPropagation();
			$fileList.outerWidth(width);
			$fileList.outerHeight(height);

			var scroll = pmease.commons.history.getScroll();
			if (scroll) {
			    $fileList.scrollLeft(scroll.left);
			    $fileList.scrollTop(scroll.top);
			}
		});

		$fileList.scroll(function() {
	    	pmease.commons.history.setScroll({left: $fileList.scrollLeft(), top: $fileList.scrollTop()});
		});

		$.ajax({
			url: lastCommitsUrl,
			cache: false, 
			success: function(lastCommits) {
				if (jQuery.contains(document, $container[0])) { // add this check to avoid rendering last commits if directory is changed
					var $table = $container.find(".file-list>table");
					$table.find("tr.child").each(function() {
						var $row = $(this);
						var path = $row.find("td.path span").text();
						var index = path.indexOf('/');
						if (index != -1)
							path = path.substring(0, index);
						var lastCommit = lastCommits[path];
						
						var html = "<img src='" + lastCommit.authorAvatarUrl + "' class='avatar'/> ";
						if (lastCommit.authorUrl)
							html += "<a href='" + lastCommit.authorUrl + "'>" + lastCommit.authorName + "</a>";
						else
							html += "<span>" + lastCommit.authorName + "</span>";
						
						$row.children(".last-commit.author").empty().append(html);
						$row.children(".last-commit.when").append("<span class='when'>" + lastCommit.when + "</span>");
						$row.children(".last-commit.message").append("<a href='" + lastCommit.url + "'>" + lastCommit.summary + "</a>");
					});
				}
			}
		});
	}
}
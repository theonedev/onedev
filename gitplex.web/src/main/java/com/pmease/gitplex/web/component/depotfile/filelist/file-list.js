gitplex.filelist = {
	init: function(containerId, lastCommitsUrl) {
		var $container = $("#" + containerId);
		var containerEl = $container[0];
		var $fileList = $container.find(">.file-list");
		
		$fileList.on("autofit", function(e, width, height) {
			e.stopPropagation();
			$fileList.outerWidth(width);
			$fileList.outerHeight(height);

			var viewState = pmease.commons.history.getViewState();
			if (viewState && viewState.scroll) {
			    $fileList.scrollLeft(viewState.scroll.left);
			    $fileList.scrollTop(viewState.scroll.top);
			}
		});
		
		$fileList.on("storeViewState", function(e) {
			e.stopPropagation();
			pmease.commons.history.setViewState({scroll:{left: $fileList.scrollLeft(), top: $fileList.scrollTop()}});			
		});

		$.ajax({
			url: lastCommitsUrl,
			cache: false, 
			beforeSend: function(xhr) {
				xhr.setRequestHeader('Wicket-Ajax', 'true');
				xhr.setRequestHeader('Wicket-Ajax-BaseURL', Wicket.Ajax.baseUrl || '.');
			},
			success: function(lastCommits) {
				if (jQuery.contains(document, containerEl)) { // add this check to avoid rendering last commits if directory is changed
					var $table = $container.find(".file-list>table");
					$table.find("tr.child").each(function() {
						var $row = $(this);
						var path = $row.find("td.path span").text();
						var index = path.indexOf('/');
						if (index != -1)
							path = path.substring(0, index);
						var lastCommit = lastCommits[path];

						if (lastCommit) {
							var html = "<img src='" + lastCommit.authorAvatarUrl + "' class='avatar'/> ";
							if (lastCommit.authorUrl)
								html += "<a href='" + lastCommit.authorUrl + "'>" + lastCommit.authorName + "</a>";
							else
								html += "<span>" + lastCommit.authorName + "</span>";
							
							$row.children(".last-commit.author").empty().append(html);
							$row.children(".last-commit.when").append("<span class='when'>" + lastCommit.when + "</span>");
							$row.children(".last-commit.message").append("<a href='" + lastCommit.url + "'>" + lastCommit.summary + "</a>");
						}
					});
				}
			}
		});
	}
};
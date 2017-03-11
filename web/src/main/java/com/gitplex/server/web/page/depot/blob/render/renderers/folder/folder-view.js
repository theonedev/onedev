gitplex.server.folderView = function(containerId, lastCommitsUrl) {
	var $container = $("#" + containerId);
	var $folderView = $container.find(">.folder-view");
	
	$.ajax({
		url: lastCommitsUrl,
		cache: false, 
		beforeSend: function(xhr) {
			xhr.setRequestHeader('Wicket-Ajax', 'true');
			xhr.setRequestHeader('Wicket-Ajax-BaseURL', Wicket.Ajax.baseUrl || '.');
		},
		success: function(lastCommits) {
			if (jQuery.contains(document, $container[0])) { // add this check to avoid rendering last commits if directory is changed
				var $table = $container.find(".folder-view>table");
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
	
	$folderView.on("getViewState", function(e) {
		return {scroll:{left: $folderView.scrollLeft(), top: $folderView.scrollTop()}};			
	});
	
	$folderView.on("setViewState", function(e, viewState) {
		if (viewState.scroll) {
		    $folderView.scrollLeft(viewState.scroll.left);
		    $folderView.scrollTop(viewState.scroll.top);
		}
	});
	
	$folderView.on("autofit", function(e, width, height) {
		$folderView.outerWidth(width);
		$folderView.outerHeight(height);
	});
};
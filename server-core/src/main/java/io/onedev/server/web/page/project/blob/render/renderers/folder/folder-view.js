onedev.server.folderView = {
	onDomReady: function(containerId, lastCommitsUrl, userDetailCallback) {
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
							if (lastCommit.author["@class"].indexOf("SystemUserIdent") != -1) {
								var html = "<span class='user'><img src='" + lastCommit.authorAvatarUrl + "' class='avatar'/> <span class='name'>OneDev</span></span>";
								$row.children(".last-commit.author").empty().append(html);
							} else {
								var html = "<a class='user'><img src='" + lastCommit.authorAvatarUrl + "' class='avatar'/> <span class='name'></span></a>";
								var $author = $row.children(".last-commit.author");
								$author.empty().append(html);
								$author.find(".name").text(lastCommit.author.name);
							}
							
							$row.children(".last-commit.when").append("<span>" + lastCommit.when + "</span>");
							var $message = $row.children(".last-commit.message");
							$message.append("<a href='" + lastCommit.url + "'></a>");
							$message.find("a").text(lastCommit.summary);

							var alignment = {targetX: 0, targetY: 0, x: 0, y: 100, offset: 8};
							$row.find("a.user").hover(function() {
								var $detail = $("<div id='user-detail' class='floating'></div>");
								$detail.hide();
								$detail.data("trigger", this);
								$detail.data("alignment", alignment);
								$("body").append($detail);
								userDetailCallback(JSON.stringify(lastCommit.author));
								return $detail;
							}, alignment);
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
	}, 

	onUserDetailAvailable: function() {
		var $userDetail = $("#user-detail");
		$userDetail.empty().append($(".user-detail-content").children()).show();
		$userDetail.align({placement: $userDetail.data("alignment"), target: {element: $userDetail.data("trigger")}});
	}
};
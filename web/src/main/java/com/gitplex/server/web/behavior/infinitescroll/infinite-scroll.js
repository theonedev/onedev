gitplex.infiniteScroll = {
	init: function(containerId, callback, loadingIndicatorUrl, pageSize) {
		var $container = $("#" + containerId);
		$container.data("callback", callback);
		$container.data("loadingIndicatorUrl", loadingIndicatorUrl);
		$container.data("page", 1);
		$container.data("pageSize", pageSize);
		$container.scroll(function() {
			gitplex.infiniteScroll.check(containerId);
		});
		setTimeout(function() {
			gitplex.infiniteScroll.check(containerId);
		}, 0);
	}, 
	check: function(containerId) {
		var $container = $("#" + containerId);
		function isInViewPort($item) {
			var tolerate = 5;
			return $item.offset().top>$container.offset().top-tolerate
					&& $item.offset().top+$item.outerHeight()<$container.offset().top+$container.height()+tolerate;
		};
		var $items;
		if ($container.is("ul")) 
			$items = $container.children();
		else
			$items = $container.find(">table>tbody>tr");
		var $lastItem = $items.last();
		var page = $container.data("page");
		var pageSize = $container.data("pageSize");
		if ($container.find(".loading-indicator").length == 0
				&& $items.length == page*pageSize && isInViewPort($lastItem)) {
			page++;
			$container.data("page", page);
			if ($container.is("ul")) {
				$container.append("<li class='loading-indicator'><img src='" + $container.data("loadingIndicatorUrl") + "'></img></li>");
			} else {
				var colspan = $lastItem.children().length;
				$lastItem.parent().append("<tr class='loading-indicator'><td colspan='" + colspan + "'><img src='" + $container.data("loadingIndicatorUrl") + "'></img></td></tr>");
			}
			$container.scrollIntoView(".loading-indicator");
			$container.data("callback")(page);
		}
	}
};

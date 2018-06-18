onedev.infiniteScroll = {
	init: function(containerId, callback, pageSize, itemSelector) {
		var $container = $("#" + containerId);
		$container.data("callback", callback);
		$container.data("page", 1);
		$container.data("pageSize", pageSize);
		$container.data("itemSelector", itemSelector);
		$container.scroll(function() {
			onedev.infiniteScroll.check(containerId);
		});
		setTimeout(function() {
			onedev.infiniteScroll.check(containerId);
		}, 0);
	}, 
	check: function(containerId) {
		var $container = $("#" + containerId);
		function isInViewPort($item) {
			var tolerate = 5;
			return $item.offset().top>$container.offset().top-tolerate
					&& $item.offset().top+$item.outerHeight()<$container.offset().top+$container.outerHeight()+tolerate;
		};
		var $items;
		if ($container.data("itemSelector"))
			$items = $container.find($container.data("itemSelector"));
		else
			$items = $container.children();
		
		var $lastItem = $items.last();
		
		var page = $container.data("page");
		var pageSize = $container.data("pageSize");
		if ($container.find(".loading-indicator").length == 0
				&& $items.length == page*pageSize && isInViewPort($lastItem)) {
			page++;
			$container.data("page", page);
			if ($container.is("ul")) {
				$container.append("<li class='loading-indicator' style='text-align:center;'><img src='/img/ajax-indicator.gif'></img></li>");
			} else if ($container.is("div")) {
				$container.append("<div class='loading-indicator' style='text-align:center;'><img src='/img/ajax-indicator.gif'></img></div>");
			} else {
				var colspan = $lastItem.children().length;
				$lastItem.parent().append("<tr class='loading-indicator'><td colspan='" + colspan + "' style='text-align:center;'><img src='/img/ajax-indicator.gif'></img></td></tr>");
			}
			$container.jumpIntoView(".loading-indicator");
			$container.data("callback")(page);
		}
	}
};

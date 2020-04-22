onedev.server.infiniteScroll = {
	onLoad: function(containerId, callback, pageSize, itemSelector) {
		var $container = $("#" + containerId);
		$container.data("callback", callback);
		$container.data("pageSize", pageSize);
		$container.data("itemSelector", itemSelector);
		var $items = onedev.server.infiniteScroll.getItems($container);
		$container.data("hasMore", $items.length >= pageSize);
		$container.scroll(function() {
			onedev.server.infiniteScroll.check(containerId);
		});
		onedev.server.infiniteScroll.check(containerId);
	}, 
	getItems: function($container) {
		if ($container.data("itemSelector"))
			return $container.find($container.data("itemSelector"));
		else
			return $container.children();
	},
	refresh: function(containerId) {
		var $container = $("#" + containerId);
		if (!$container.data("appending")) {
			$container.data("scrollTop", $container.scrollTop());
			var $items = onedev.server.infiniteScroll.getItems($container);
			$items.remove();
			$container.scrollTop(0);
			$container.data("prevItems", 0);
			var pageSize = $container.data("pageSize");
			$container.data("callback")(0, pageSize);
			$container.data("appending", true);
		}
	},
	check: function(containerId) {
		var $container = $("#" + containerId);
		function isInViewPort($item) {
			var tolerate = 5;
			return $item.offset().top>$container.offset().top-tolerate
					&& $item.offset().top+$item.outerHeight()<$container.offset().top+$container.outerHeight()+tolerate;
		};

		var $items = onedev.server.infiniteScroll.getItems($container);
		var $lastItem = $items.last();
		
		if (!$container.data("appending")) {
			var scrollTop = $container.data("scrollTop");
			if ($container.data("hasMore") && $lastItem.length != 0) {
				if (isInViewPort($lastItem)) {
					var pageSize = $container.data("pageSize");
					$container.data("prevItems", $items.length);
					$container.data("callback")($items.length, pageSize);
					$container.data("appending", true);
					if ($lastItem.is("li")) {
						$lastItem.after("<li class='loading-indicator' style='text-align:center;'><img src='/img/ajax-indicator.gif'></img></li>");
					} else if ($lastItem.is("tr")) {
						var colspan = $lastItem.children().length;
						$lastItem.after("<tr class='loading-indicator'><td colspan='" + colspan + "' style='text-align:center;'><img src='/img/ajax-indicator.gif'></img></td></tr>");
					} else {
						$lastItem.after("<div class='loading-indicator' style='text-align:center;'><img src='/img/ajax-indicator.gif'></img></div>");
					}		
					$container.find(".loading-indicator").scrollIntoView();					
				} else if (scrollTop) {
					if (scrollTop > $container.scrollTop()) 
						$container.scrollTop(scrollTop);
					else
						$container.removeData("scrollTop");
				} 
			} else if (scrollTop) {
				$container.removeData("scrollTop");
				$container.scrollTop(scrollTop);
			}
		}
	}, 
	onAppended: function(containerId) {
		var $container = $("#" + containerId);
		$container.data("appending", false);
		$container.find(".loading-indicator").remove();
		var $items = onedev.server.infiniteScroll.getItems($container);
		$container.data("hasMore", $items.length - $container.data("prevItems") >= $container.data("pageSize"));
		onedev.server.infiniteScroll.check(containerId);
		$(window).resize();
	}
};

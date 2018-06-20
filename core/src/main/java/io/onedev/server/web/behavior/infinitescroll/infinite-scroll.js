onedev.server.infiniteScroll = {
	init: function(containerId, callback, pageSize, itemSelector) {
		var $container = $("#" + containerId);
		$container.data("callback", callback);
		$container.data("pageSize", pageSize);
		$container.data("itemSelector", itemSelector);
		$container.data("hasMore", true);
		$container.scroll(function() {
			onedev.server.infiniteScroll.check(containerId);
		});
		setTimeout(function() {
			onedev.server.infiniteScroll.check(containerId);
		}, 0);
	}, 
	getItems: function($container) {
		if ($container.data("itemSelector"))
			return $container.find($container.data("itemSelector"));
		else
			return $container.children();
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
		
		var pageSize = $container.data("pageSize");
		if ($container.find(".loading-indicator").length == 0 && $container.data("hasMore") 
				&& $lastItem.length != 0 && isInViewPort($lastItem)) {
			$container.data("prevItems", $items.length);
			$container.data("callback")($items.length, pageSize);
		}
	}, 
	onAppended: function(containerId) {
		var $container = $("#" + containerId);
		var $items = onedev.server.infiniteScroll.getItems($container);
		$container.data("hasMore", $items.length - $container.data("prevItems") >= $container.data("pageSize"));
		onedev.server.infiniteScroll.check(containerId);
	}
};

onedev.server.sideInfo = {
    onDomReady: function(containerId) {
        var $container = $("#" + containerId);
		if (!onedev.server.util.isDevice()) {
			var ps = new PerfectScrollbar($container[0]);
			$container.data("ps", ps);
			$(window).resize(function() {
				ps.update();
			});
		}
    }
}
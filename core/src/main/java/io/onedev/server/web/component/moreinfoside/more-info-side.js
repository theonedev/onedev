onedev.server.moreInfoSide = {
    onDomReady: function(containerId) {
        var $container = $("#" + containerId);
		if (!onedev.server.util.isDevice()) {
			var ps = new PerfectScrollbar($container[0]);
			$(window).resize(function() {
				ps.update();
			});
		}
    }
}
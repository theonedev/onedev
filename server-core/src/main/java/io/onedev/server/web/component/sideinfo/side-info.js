onedev.server.sideInfo = {
	cookieKey: "sideInfo.visible",

	isOverlayMode: function($container) {
		return window.matchMedia("(max-width: 1198px)").matches
				|| $container.closest(".hide-side-info").length != 0;
	},

	syncTrigger: function($container) {
		$("body").toggleClass("side-info-visible", !$container.hasClass("closed"));
	},

	close: function($container) {
		$container.addClass("closed");
		onedev.server.sideInfo.syncTrigger($container);
		if (!onedev.server.sideInfo.isOverlayMode($container))
			Cookies.set(onedev.server.sideInfo.cookieKey, false, {expires: Infinity});
		setTimeout(function(){$(window).resize();}, 350);
	},

	open: function($container) {
		$container.removeClass("closed");
		onedev.server.sideInfo.syncTrigger($container);
		if (!onedev.server.sideInfo.isOverlayMode($container))
			Cookies.set(onedev.server.sideInfo.cookieKey, true, {expires: Infinity});
		setTimeout(function(){$(window).resize();}, 350);
	},

    onDomReady: function(containerId) {
        var $container = $("#" + containerId);

        // Wide pinned layouts restore from cookie (default open); overlay stays closed until opened
        if (!onedev.server.sideInfo.isOverlayMode($container)
                && Cookies.get(onedev.server.sideInfo.cookieKey) != "false")
			$container.removeClass("closed");
		onedev.server.sideInfo.syncTrigger($container);

		$(document).on("mouseup touchstart", function(e) {
			if (!onedev.server.sideInfo.isOverlayMode($container))
				return;
			if ($(".flatpickr-calendar.open").length == 0 
					&& $(".pcr-app.visible").length == 0 
					&& $container.find(".dropdown-open").length == 0
					&& $(".select2-drop:visible").length == 0) {
				var x = e.pageX;
				var y = e.pageY;
				
				var contains = $container.offset().left<x && $container.offset().left+$container.outerWidth()>x
						&& $container.offset().top<y && $container.offset().top+$container.outerHeight()>y;
						
			    if (!$container.is(e.target) && $container.has(e.target).length === 0 && !contains) {
                    onedev.server.sideInfo.close($container);
			    }
			}            
        });
        $(document).on("keydown", function(e) {
            if (onedev.server.sideInfo.isOverlayMode($container)
                    && e.keyCode == 27 && $(e.target).closest(".flatpickr-calendar").length == 0
                    && $container.find(".dropdown-open").length == 0
                    && $(".select2-drop:visible").length == 0 
                    && $(".flatpickr-calendar.open").length == 0
                    && $(".pcr-app.visible").length == 0) {
                onedev.server.sideInfo.close($container);
            }
        });
    }
}

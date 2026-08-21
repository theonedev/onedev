onedev.server.sideInfo = {
	cookieKey: "sideInfo.visible",

	isOverlayMode: function($container) {
		return window.matchMedia("(max-width: 1198px)").matches
				|| $container.closest(".hide-side-info").length != 0;
	},

	syncTrigger: function($container) {
		$("body").toggleClass("side-info-visible", !$container.hasClass("closed"));
	},

	dockMoreInfoWithStickyTabs: function(options) {
		var $root = $(options.root);
		var sentinel = $root.find(options.stickySentinel)[0];
		var $row = $root.find(options.stickyRow);
		if (!$root.length || !sentinel || !$row.length)
			return;

		var oldObserver = $row.data("moreInfoDockObserver");
		if (oldObserver)
			oldObserver.disconnect();

		var scrollRoot = $row.closest(".autofit")[0] || null;
		var intersectionObserver = new IntersectionObserver(function(entries) {
			var entry = entries[0];
			var aboveScrollport = entry.rootBounds
				&& entry.boundingClientRect.bottom <= entry.rootBounds.top;
			$row.toggleClass("is-docked", !entry.isIntersecting && aboveScrollport);
		}, {root: scrollRoot, threshold: 0});
		intersectionObserver.observe(sentinel);
		$row.data("moreInfoDockObserver", intersectionObserver);
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
		var overlayMode = onedev.server.sideInfo.isOverlayMode($container);
		var mql = window.matchMedia("(max-width: 1198px)");

		// Wide pinned layouts restore from cookie (default open); overlay stays closed until opened
		if (!overlayMode && Cookies.get(onedev.server.sideInfo.cookieKey) != "false") {
			$container.addClass("no-animation").removeClass("closed");
			requestAnimationFrame(function(){$container.removeClass("no-animation");});		
		}
		onedev.server.sideInfo.syncTrigger($container);
		$("html").addClass("side-info-ready");

		function onBreakpointChange() {
			var nextOverlayMode = onedev.server.sideInfo.isOverlayMode($container);
			if (nextOverlayMode == overlayMode)
				return;
			overlayMode = nextOverlayMode;
			if (overlayMode) {
				// Entering overlay: close without overwriting the pinned visibility cookie
				$container.addClass("closed");
				onedev.server.sideInfo.syncTrigger($container);
				setTimeout(function(){$(window).resize();}, 350);
			} else if (Cookies.get(onedev.server.sideInfo.cookieKey) != "false") {
				onedev.server.sideInfo.open($container);
			} else {
				onedev.server.sideInfo.close($container);
			}
		}
		if (mql.addEventListener)
			mql.addEventListener("change", onBreakpointChange);
		else if (mql.addListener)
			mql.addListener(onBreakpointChange);

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

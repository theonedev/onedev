onedev.server.sidebar = {
	onDomReady: function(containerId, miniCookieKey) {
		var $sidebar = $("#"+containerId);
		var $tabs = $sidebar.children(".tabs");
		var $miniToggle = $sidebar.children(".mini-toggle");

		var ps = new PerfectScrollbar($tabs[0]);
		
		$(window).resize(function() {
			ps.update();
		});
	
		function onMiniToggled() {
			var $links = $sidebar.find("ul>li>a");
			if ($sidebar.hasClass("minimized")) {
				$links.each(function() {
					$(this).attr("title", $(this).find(".text").text());
				});
				$miniToggle.attr("title", "Expand");
			} else {
				$links.removeAttr("title");
				$miniToggle.attr("title", "Collapse");
			}
		}

		$miniToggle.click(function() {
			$sidebar.toggleClass("minimized");
			if (miniCookieKey) {
				var cookieValue = $sidebar.hasClass("minimized")?"yes":"no";
				Cookies.set(miniCookieKey, cookieValue, {expires: Infinity});
			}
			onMiniToggled();
			$(window).resize();
		});

		onMiniToggled();
	}
}
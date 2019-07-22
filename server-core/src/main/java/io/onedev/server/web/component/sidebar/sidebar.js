onedev.server.sidebar = {
	onDomReady: function(containerId, miniCookieKey) {
		var $sidebar = $("#"+containerId);
		var $tabs = $sidebar.children(".tabs");
		var $miniToggle = $sidebar.children(".mini-toggle");

		// Sometimes we have sidebar positioned absolutely in order not to occupy
		// screen estates on mobile devices. In this case, we also disable the 
		// perfect scrollbar as container applied with perfect scrollbar can not 
		// be panned on mobile      
		if ($sidebar.css("position") == "fixed") {
			var ps = new PerfectScrollbar($tabs[0]);
			
			$(window).resize(function() {
				ps.update();
			});
		}
	
		function onMiniToggled() {
			var $links = $sidebar.find("ul>li>a");
			if ($sidebar.hasClass("minimized")) {
				$links.each(function() {
					$(this).attr("title", $(this).find(".text").text());
				});
			} else {
				$links.removeAttr("title");
			}
		}

		$miniToggle.click(function() {
			$sidebar.toggleClass("minimized", 100, function() {
				if (miniCookieKey) {
					var cookieValue = $sidebar.hasClass("minimized")?"yes":"no";
					Cookies.set(miniCookieKey, cookieValue, {expires: Infinity});
				}
				onMiniToggled();
				$(window).resize();
			});
		});

		onMiniToggled();
	}
}
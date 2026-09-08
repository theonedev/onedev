onedev.server.wiki = {
	init: function(navigationId, navigate, initialState) {
		var $navigation = $("#" + navigationId);
		var $view = $navigation.parent();
		var width = parseInt(Cookies.get("wiki.navigation.width"));
		if (!Number.isFinite(width))
			width = 300;
		$navigation.width(Math.max(160, Math.min(width, $view.width() - 80)));
		$navigation.resizable({
			autoHide: false,
			handles: {w: $navigation.children(".ui-resizable-handle")},
			minWidth: 160,
			start: function() {
				$(this).resizable("option", "maxWidth", Math.max(160, $view.width() - 300));
			},
			stop: function(e, ui) {
				Cookies.set("wiki.navigation.width", ui.size.width, {expires: Infinity});
			}
		});
		var pages = new Map();
		$navigation.find("a[data-wiki-page]").each(function() {
			var url = new URL(this.href);
			pages.set(url.origin + url.pathname + url.search, $(this).attr("data-wiki-page"));
		});
		// Delegate from the view so links in Ajax-replaced content also update the page and sidebar.
		$view.off("click.wikiNavigation").on("click.wikiNavigation", "a", function(e) {
			if (e.isDefaultPrevented() || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey
					|| e.which > 1 || this.target || this.hasAttribute("download"))
				return;
			var url = new URL(this.href);
			var destination = pages.get(url.origin + url.pathname + url.search);
			if (destination === undefined)
				return;
			e.preventDefault();
			var currentPage = $navigation.find("a[data-wiki-page][aria-current='page']")
					.attr("data-wiki-page");
			if (destination === currentPage) {
				history.replaceState(history.state, "", url.href);
				onedev.server.wiki.scrollToHeading(url.hash);
				return;
			}
			onedev.server.viewState.getFromViewAndSetToHistory();
			navigate(destination, url.hash);
		});
		var $body = $(".wiki>.body");
		$body.off("getViewState.wiki setViewState.wiki")
			.on("getViewState.wiki", function() {
				var $content = $(".wiki-content");
				return {scroll: {left: $content.scrollLeft(), top: $content.scrollTop()}};
			})
			.on("setViewState.wiki", function(e, state) {
				if (state.scroll)
					$(".wiki-content").scrollLeft(state.scroll.left).scrollTop(state.scroll.top);
			});
		if (!history.state || !history.state.data)
			onedev.server.history.replaceState(window.location.href, initialState, document.title);
		$(window).resize();
	},
	onPageChanged: function(page) {
		var $navigation = $(".wiki-navigation");
		$navigation.find("a[data-wiki-page]").each(function() {
			if ($(this).attr("data-wiki-page") === page)
				$(this).attr("aria-current", "page");
			else
				$(this).removeAttr("aria-current");
		});
		$(window).resize();
	},
	scrollToHeading: function(hash, waitForImages) {
		var request = this.headingRequest = (this.headingRequest || 0) + 1;
		$(".wiki-content").scrollTop(0).scrollLeft(0);
		if (hash) {
			var name;
			try {
				name = decodeURIComponent(hash.substring(1));
			} catch (e) {
				return;
			}
			var $heading = $(".wiki-content [id], .wiki-content a[name]").filter(function() {
				return this.id === name || this.getAttribute("name") === name;
			}).first();
			if ($heading.length) {
				if (waitForImages) {
					// Lazy images above the heading can change its position after the Ajax response.
					var loader = lozad();
					var images = $(".wiki-content img").filter(function() {
						return this.compareDocumentPosition($heading[0]) & Node.DOCUMENT_POSITION_FOLLOWING;
					}).get();
					Promise.all(images.map(function(image) {
						return new Promise(function(resolve) {
							function done() {
								image.removeEventListener("load", done);
								image.removeEventListener("error", done);
								resolve();
							}
							image.addEventListener("load", done);
							image.addEventListener("error", done);
							loader.triggerLoad(image);
							if (image.complete)
								done();
						});
					})).then(function() {
						if (request === onedev.server.wiki.headingRequest && $heading[0].isConnected)
							$heading[0].scrollIntoView();
					});
				} else {
					$heading[0].scrollIntoView();
				}
			}
		}
	}
};

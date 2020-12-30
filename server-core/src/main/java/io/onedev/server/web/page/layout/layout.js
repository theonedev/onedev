onedev.server.layout = {
	onDomReady: function() {
		var $body = $("body");
		var $sidebar = $(".sidebar");
		var $sidebarBody = $(".sidebar-body");
		
		var sidebarHideBreakpoint = 992;
		
		var resizeTimer;
		$(window).resize(function() {
			if (window.innerWidth >= sidebarHideBreakpoint)
				$sidebar.removeClass("sidebar-docked");
  			$body.addClass("no-animation");
  			clearTimeout(resizeTimer);
  			resizeTimer = setTimeout(function() {
    			$body.removeClass("no-animation");
  			}, 300);
		});

		$(".sidebar-mini-toggle").click(function() {
			$sidebar.toggleClass("sidebar-minimized");
			Cookies.set("sidebar.minimized", $sidebar.hasClass("sidebar-minimized"));
			setTimeout(function() {
				$(window).resize();
			}, 300);
		});
		$(".sidebar-toggle").click(function() {
			$sidebar.addClass("sidebar-docked");
		});
		$(".sidebar-close").click(function() {
			$sidebar.removeClass("sidebar-docked");
		});

		$(document).on("mouseup touchstart", function(e) {
		    if (!$sidebar.is(e.target) && $sidebar.has(e.target).length === 0)
				$sidebar.removeClass("sidebar-docked");
		});
		
		$(document).on("keydown", function(e) {
			if (e.keyCode == 27) 
				$sidebar.removeClass("sidebar-docked");
		});
		
		$(".sidebar-menu .menu-toggle").click(function() {
			if (window.innerWidth < sidebarHideBreakpoint || !$(".sidebar").hasClass("sidebar-minimized")) {
				var $this = $(this);
				$this.toggleClass("open");
				$this.next().slideToggle(300, function() {
					$sidebarBody.trigger("resized");
					$this.parent().scrollIntoView($sidebarBody[0], true);
				});
			}
		});
		
		$sidebar.find(".menu-link:not('.menu-toggle')").click(function() {
			window.sessionStorage.setItem("onedev.sidebar.scrollTop", $sidebarBody.scrollTop());	
		});		

		var processTimer;		
		function processSubMenus($rightmost) {
			if (window.innerWidth >= sidebarHideBreakpoint && $(".sidebar").hasClass("sidebar-minimized")) {
				clearTimeout(processTimer);
				processTimer = setTimeout(function() {
					$(".sidebar-menu .menu-body .menu-body").each(function() {
						if ($rightmost.closest(this).length == 0) {
							var $this = $(this);
							$this.removeAttr("style").removeClass("ps ps-scroll").hide().prev(".menu-toggle").removeClass("open");	
							var ps = $this.data("ps");
							if (ps) {
								ps.destroy();
								$this.removeData("ps");
							}
						}
					});
					if (!$rightmost.is(":visible")) {
						$rightmost.show().css({
							"position": "fixed", 
							"background": "white",
							"box-shadow": "0px 0px 8px 0px rgba(0,0,0,0.1)", 
							"max-height": ($(window).height() - 50) + "px",
							"border-radius": "0.42rem", 
							"padding": "0.5rem 0"
						});
						var $parent = $rightmost.parent().closest(".menu-body");
						var $toggler = $rightmost.prev(".menu-toggle");
						$toggler.addClass("open");
						var left = $parent.offset().left + $parent.outerWidth(); 
						var top = $toggler.offset().top - $(window).scrollTop();
						if (top + $rightmost.outerHeight() > $(window).height())
							top = $(window).height() - $rightmost.outerHeight() - 25;
						$rightmost.css("left", left).css("top", top);
						$rightmost.addClass("ps ps-scroll");
						var ps = new PerfectScrollbar($rightmost[0]);
						ps.update();
						$rightmost.data("ps", ps);
					}
				}, 200);
			}
		}

		$(".sidebar-menu .menu-toggle").mouseover(function() {
			processSubMenus($(this).next());
			return false;
		});
		$(".sidebar-menu .menu-body").mouseover(function() {
			processSubMenus($(this));		
			return false;	
		});
		$(".sidebar-menu .menu-body").mouseout(function() {
			processSubMenus($(".sidebar-menu .menu-body").first());
			return false;
		});		

		var $main = $body.children(".main");

		$main.on("getViewState", function() {
		    return {
				scrollLeft: $main.scrollLeft(),
				scrollTop: $main.scrollTop()
			}
		});
		$main.on("setViewState", function(e, viewState) {
		    $main.scrollLeft(viewState.scrollLeft);
			$main.scrollTop(viewState.scrollTop);
		});
		
		$main.on("resized", function() {
			var count = $(".need-width:visible:not(:has('.need-width:visible'))").length;
			if (count != 0) {
				var minWidth = 600 + 300*count;
				$main.css("min-width", minWidth + "px");
			} else {
				$main.css("min-width", "");
			}
			return false;
		});
	},
	onLoad: function() {
		var $sidebarBody = $(".sidebar-body");
		var scrollTop = window.sessionStorage.getItem("onedev.sidebar.scrollTop");
		var currentMenuLinkCount = $(".sidebar-menu .menu-link").length;
		if (scrollTop) {
			window.sessionStorage.removeItem("onedev.sidebar.scrollTop");
			$sidebarBody.scrollTop(scrollTop);	
		} else {
			var menuChanged = (currentMenuLinkCount != window.sessionStorage.getItem("onedev.sidebar.menuLinkCount"));
			$(".sidebar-menu .menu-link.active:visible").last()
					.scrollIntoView(document.querySelector(".sidebar-body"), menuChanged);
		}
		window.sessionStorage.setItem("onedev.sidebar.menuLinkCount", currentMenuLinkCount);
	}
}
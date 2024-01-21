onedev.server.layout = {
	onDomReady: function(commandPaletteCallback) {
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
					$this.parent()[0].scrollIntoViewIfNeeded();
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
							$this.removeAttr("style").removeClass("ps ps-scroll sidebar-dropdown").hide().prev(".menu-toggle").removeClass("open");	
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
						}).addClass("sidebar-dropdown");
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
		
		if (onedev.server.util.isMac()) {
			$("a.command-palette").attr("title", "cmd-k to show command palette").html("<span class='keycap'>⌘</span> <span class='keycap'>k</span>");
		}
		else
			$("a.command-palette").attr("title", "ctrl-k to show command palette").html("<span class='keycap'>ctrl</span> <span class='keycap'>k</span>");
		$(document).keydown(function(e) {
			if (e.keyCode == 75 && (e.ctrlKey || e.metaKey) && !e.shiftKey && $('div.command-palette').length == 0) { // cmd+k
				commandPaletteCallback();
				e.preventDefault();
				return false;
			}
		});
	},
	onLoad: function() {
		var $sidebarBody = $(".sidebar-body");
		var scrollTop = window.sessionStorage.getItem("onedev.sidebar.scrollTop");
		if (scrollTop) {
			window.sessionStorage.removeItem("onedev.sidebar.scrollTop");
			$sidebarBody.scrollTop(scrollTop);	
		} else {
			var $lastActive = $(".sidebar-menu .menu-link.active:visible").last();
			if ($lastActive.length != 0)
				$lastActive[0].scrollIntoViewIfNeeded();
		}
	},
	onNewVersionStatusIconLoaded: function(newVersionStatusCallback) {
		var $newVersionStatusIcon = $(".new-version-status>img");
		var width = $newVersionStatusIcon[0].width;
		var height = $newVersionStatusIcon[0].height;

		var newVersionStatus;
		if (width == 17 && height == 17)
			newVersionStatus = "none";
		else if (width == 16 && height == 16)
			newVersionStatus = "normal";
		else if (width == 17 && height == 16)
			newVersionStatus = "warning";
		else
			newVersionStatus = "danger";

		if (newVersionStatus == "none") {
			$newVersionStatusIcon.parent().hide();
		} else {
			$newVersionStatusIcon.css({
				width: "16px",
				height: "16px"
			});
		}
		
		// cache new version status so that we do not need to check new version 
		// in remaining time of the day
		if (newVersionStatusCallback)
			newVersionStatusCallback(newVersionStatus);
	}
}
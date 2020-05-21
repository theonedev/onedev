onedev.server.pullRequestDetail = {
	onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $main = $("#request-detail>.main");
		function adjustHeight() {
			$main.outerHeight($(window).height() - $main.offset().top);
		}
		adjustHeight();
		$main.on("resized", adjustHeight);
		$main.scroll(function() {
			$main.find(".scroll-aware").addBack(".scroll-aware").trigger("scrolled");
		});
	},
	onSummaryDomReady: function() {
		var $summary = $("#request-detail>.main>.summary");
		
		$summary.find(".more-info").click(function() {
			$("#request-detail>.main>.status-and-branches .more-info").trigger("click");
		});
		
		function modifyMoreInfoLink() {
			$summary.find(".more-info").each(function() {
				if ($("#request-detail>.main>.status-and-branches .more-info").is(":visible"))
					$(this).removeClass("not-link");
				else 
					$(this).addClass("not-link");
			});
		}
		modifyMoreInfoLink();
		$summary.on("resized", modifyMoreInfoLink);
	}
}
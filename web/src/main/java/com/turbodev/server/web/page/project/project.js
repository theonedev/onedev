turbodev.server.project = function() {
	var $nav = $("#project>.sidebar>table>tbody>tr>td.nav");
	
	function enableTooltip() {
		$nav.find("li a").each(function() {
			$(this).attr("title", $(this).find(".text").text());
		});
	}
	
	var cookieKey = "project.miniSidebar";
	if (Cookies.get(cookieKey) === "yes")
		enableTooltip();
	
	$nav.find(">.mini-toggle").click(function() {
		$nav.toggleClass("mini");
		if ($nav.hasClass("mini")) {
			Cookies.set(cookieKey, "yes", {expires: Infinity});
			enableTooltip();
		} else {
			Cookies.set(cookieKey, "no", {expires: Infinity});
			$nav.find("li a").removeAttr("title");
		}
		$(window).resize();
	});
};
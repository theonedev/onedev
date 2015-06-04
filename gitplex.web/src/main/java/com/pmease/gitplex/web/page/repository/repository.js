$(function() {
	var $nav = $("#repository>.body>.sidebar>table>tbody>tr>td.nav");
	
	function enableTooltip() {
		$nav.find("li a").each(function() {
			$(this).tooltip({
				title: $(this).find(".text").text(), 
				placement: "right"
			});
		});
	}
	
	var cookieKey = "repository.miniSidebar";
	if (Cookies.get(cookieKey) === "yes") {
		$nav.addClass("mini");
		enableTooltip();
	}
	
	$nav.find(">.mini-toggle").click(function() {
		$nav.toggleClass("mini");
		if ($nav.hasClass("mini")) {
			Cookies.set(cookieKey, "yes", {expires: Infinity});
			enableTooltip();
		} else {
			Cookies.set(cookieKey, "no", {expires: Infinity});
			$nav.find("li a").tooltip("destroy");
		}
		$(window).resize();
	});
	
});
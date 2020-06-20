onedev.server.buildDetail = {
    onErrorMessageDomReady: function() {
        var ps = new PerfectScrollbar($("#build-detail>.main>.error-message")[0]);
        $(window).resize(function() {
            ps.update();
        });
    },
    onDomReady: function() {
		$("body").css("overflow", "hidden");
		var $main = $("#build-detail>.main");
		function adjustHeight() {
			$main.outerHeight($(window).height() - $main.offset().top);
		}
		adjustHeight();
		$main.addClass("resize-aware").on("resized", adjustHeight);
    },
    onLogDomReady: function() {
		var $main = $("#build-detail>.main");
		$main.css("overflow", "hidden");
        var $log = $main.find(">div>.build-log");
        function setLogHeight() {
            $log.height($(window).height() - $log.offset().top - 40);
        }
        $(window).resize(setLogHeight);
        setLogHeight();
    }
}
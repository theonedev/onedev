onedev.server.buildDetail = {
    onErrorMessageDomReady: function() {
        var ps = new PerfectScrollbar($("#build-detail>.main>.error-message")[0]);
        $(window).resize(function() {
            ps.update();
        });
    },
    onLogDomReady: function() {
        var $log = $("#build-detail>.main>div>.build-log");
        function setLogHeight() {
            $log.height($(window).height() - $log.offset().top - 40);
        }
        $(window).resize(setLogHeight);
        setLogHeight();
    }
}
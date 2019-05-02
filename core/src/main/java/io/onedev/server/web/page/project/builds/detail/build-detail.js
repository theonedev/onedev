onedev.server.buildDetail = {
    onStatusMessageDomReady: function() {
        var ps = new PerfectScrollbar($("#build-detail>.main>.status-message")[0]);
        $(window).resize(function() {
            ps.update();
        });
    },
    onLogDomReady: function() {
        var $log = $("#build-detail>.main>div>.build-log");
        function calcLogHeight() {
            $log.height($(window).height() - $log.offset().top - 40);
        }
        $(window).resize(calcLogHeight);
        calcLogHeight();
    }
}
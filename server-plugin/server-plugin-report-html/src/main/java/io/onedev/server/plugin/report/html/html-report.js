onedev.server.htmlReport = {
    onWindowLoad: function(htmlReportId) {
        function setReportHeight() {
            var $htmlReport = $("#" + htmlReportId);
            $htmlReport.height($(window).height() - $htmlReport.offset().top - 20);
        }
        setReportHeight();
        $(window).resize(setReportHeight);
    }
}
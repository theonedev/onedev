onedev.server.commitJobs = {
    onDomReady: function(containerId) {
        $("#" + containerId + ">.commit-jobs>.job>.body").each(function() {
            var ps = new PerfectScrollbar(this);
            $(window).resize(function() {
                ps.update();
            });
        });
        
    }
}
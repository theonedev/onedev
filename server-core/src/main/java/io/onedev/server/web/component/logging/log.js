onedev.server.log = {
    appendLogEntries: function(containerId, logEntries) {
        var $log = $("#" + containerId + ">.log");
		// parent scrollbar jumps as we remove/add log entries in chrome. Let's restore it afterwards
		$log.parents().each(function() {
			$(this).data("logScrollLeft", $(this).scrollLeft());
			$(this).data("logScrollTop", $(this).scrollTop());
		});
        for (var i=0; i<logEntries.length; i++)
			$log.append(onedev.server.jobLogEntry.render(logEntries[i], true));
        
        var $entries = $log.children(".log-entry");
        
		var maxNumOfEntries = $log.data("maxNumOfEntries");
        var numOfEntriesToRemove = $entries.length - maxNumOfEntries;
        if (numOfEntriesToRemove > 0) {
            $entries.slice(0, numOfEntriesToRemove).remove();
            if ($log.children(".too-many-entries").length == 0)
                $log.prepend("<h6 class='too-many-entries text-info'>Too many logs, displaying recent " + maxNumOfEntries + "</h6>")
        } 
        if ($entries.length == 0) {
            if ($log.children(".no-entries").length == 0)
                $log.prepend("<h6 class='no-entries text-info'>No logs</h6>");    
        } else {
            $log.children(".no-entries").remove();
        }
		$log.trigger("resized");
        $log.scrollTop($log[0].scrollHeight);
		$log.parents().each(function() {
			$(this).scrollLeft($(this).data("logScrollLeft"));
			$(this).scrollTop($(this).data("logScrollTop"));
		});
    }, 
	pauseUpdated: function(containerId, paused) {
		var $log = $("#" + containerId + ">.log");
		$log.find(".script-paused").remove();
		if (paused) {
			var $paused = $("<div class='script-paused font-weight-bolder border rounded px-2 py-1 mt-2 border-warning text-warning'>Execution paused</div>");		
			$log.append($paused);
			var resumeCallback = $log.data("resumeCallback");
			if (resumeCallback) {
				var $resumeLink = $("<a class='ml-2'>Resume</a>");
				$paused.append($resumeLink);
				$resumeLink.click(resumeCallback);
			}
			$paused[0].scrollIntoViewIfNeeded();
		}
	},
	onDomReady: function(containerId, logEntries, maxNumOfEntries, paused, resumeCallback) {
        var $log = $("#" + containerId + ">.log");
		$log.data("resumeCallback", resumeCallback);		
		$log.data("maxNumOfEntries", maxNumOfEntries);
		onedev.server.log.appendLogEntries(containerId, logEntries);
		onedev.server.log.pauseUpdated(containerId, paused);
	}
	
}


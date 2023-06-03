onedev.server.buildLog = {
    appendLogEntries: function(containerId, logEntries) {
        var $buildLog = $("#" + containerId + ">.build-log");
		// parent scrollbar jumps as we remove/add log entries in chrome. Let's restore it afterwards
		$buildLog.parents().each(function() {
			$(this).data("buildLogScrollLeft", $(this).scrollLeft());
			$(this).data("buildLogScrollTop", $(this).scrollTop());
		});
        for (var i=0; i<logEntries.length; i++)
			$buildLog.append(onedev.server.jobLogEntry.render(logEntries[i], true));
        
        var $entries = $buildLog.children(".log-entry");
        
		var maxNumOfEntries = $buildLog.data("maxNumOfEntries");
        var numOfEntriesToRemove = $entries.length - maxNumOfEntries;
        if (numOfEntriesToRemove > 0) {
            $entries.slice(0, numOfEntriesToRemove).remove();
            if ($buildLog.children(".too-many-entries").length == 0)
                $buildLog.prepend("<h6 class='too-many-entries text-info'>Too many logs, displaying recent " + maxNumOfEntries + "</h6>")
        } 
        if ($entries.length == 0) {
            if ($buildLog.children(".no-entries").length == 0)
                $buildLog.prepend("<h6 class='no-entries text-info'>No logs</h6>");    
        } else {
            $buildLog.children(".no-entries").remove();
        }
		$buildLog.trigger("resized");
        $buildLog.scrollTop($buildLog[0].scrollHeight);
		$buildLog.parents().each(function() {
			$(this).scrollLeft($(this).data("buildLogScrollLeft"));
			$(this).scrollTop($(this).data("buildLogScrollTop"));
		});
    }, 
	buildUpdated: function(containerId, paused) {
		var $buildLog = $("#" + containerId + ">.build-log");
		$buildLog.find(".script-paused").remove();
		if (paused) {
			var $paused = $("<div class='script-paused font-weight-bolder border rounded px-2 py-1 mt-2 border-warning text-warning'>Script execution paused</div>");		
			$buildLog.append($paused);
			var resumeCallback = $buildLog.data("resumeCallback");
			if (resumeCallback) {
				var $resumeLink = $("<a class='ml-2'>Resume</a>");
				$paused.append($resumeLink);
				$resumeLink.click(resumeCallback);
			}
			$paused[0].scrollIntoViewIfNeeded();
		}
	},
	onDomReady: function(containerId, logEntries, maxNumOfEntries, buildPaused, resumeCallback) {
        var $buildLog = $("#" + containerId + ">.build-log");
		$buildLog.data("resumeCallback", resumeCallback);		
		$buildLog.data("maxNumOfEntries", maxNumOfEntries);
		onedev.server.buildLog.appendLogEntries(containerId, logEntries);
		onedev.server.buildLog.buildUpdated(containerId, buildPaused);
	}
}


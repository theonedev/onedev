onedev.server.buildLog = {
    appendLogEntries: function(containerId, logEntries, maxNumOfEntries) {
        var $buildLog = $("#" + containerId + ">.build-log");

        for (var i=0; i<logEntries.length; i++)
			$buildLog.append(onedev.server.jobLogEntry.render(logEntries[i], true));
        
        var $entries = $buildLog.children(".log-entry");
        
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
    }

}


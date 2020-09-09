onedev.server.buildLog = {
    renderLogEntry: function(logEntry) {
		var $logEntry = $("<div class='log-entry'></div>");
		$logEntry.append("<span class='date mr-3'>" + moment(logEntry.date).format("HH:mm:ss") + "</span>");
		var $message = $("<span class='message'></span>");
		$message.text(logEntry.message);
		$logEntry.append($message); 
		return $logEntry;
    },
    appendLogEntries: function(containerId, logEntries, maxNumOfLogEntries) {
        var $buildLog = $("#" + containerId + ">.build-log");

        for (var i=0; i<logEntries.length; i++)
            $buildLog.append(onedev.server.buildLog.renderLogEntry(logEntries[i]));
        
        var $logEntries = $buildLog.children(".log-entry");
        
        var numOfEntriesToRemove = $logEntries.length - maxNumOfLogEntries;
        if (numOfEntriesToRemove > 0) {
            $logEntries.slice(0, numOfEntriesToRemove).remove();
            if ($buildLog.children(".too-many-entries").length == 0)
                $buildLog.prepend("<h6 class='too-many-entries text-warning'>Too many log entries, displaying recent " + maxNumOfLogEntries + "</h6>")
        } 
        if ($logEntries.length == 0) {
            if ($buildLog.children(".no-entries").length == 0)
                $buildLog.prepend("<h6 class='no-entries text-warning'>No log entries</h6>");    
        } else {
            $buildLog.children(".no-entries").remove();
        }
		$buildLog.trigger("resized");
        $buildLog.scrollTop($buildLog[0].scrollHeight);
    }

}
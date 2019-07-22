onedev.server.buildLog = {
    onDomReady: function(containerId, logEntries, maxNumOfLogEntries) {
        var $buildLog = $("#" + containerId + ">.build-log");
        var ps = new PerfectScrollbar($buildLog[0]);
        $(window).resize(function() {
            ps.update();
        });
        onedev.server.buildLog.appendLogEntries(containerId, logEntries, maxNumOfLogEntries);
    },
    renderLogEntry: function(logEntry) {
		var $logEntry = $("<div class='log-entry'></div>");
		$logEntry.append("<span class='date'>" + moment(logEntry.date).format("HH:mm:ss") + "</span>");
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
                $buildLog.prepend("<div class='too-many-entries'>Too many log entries, displaying recent " + maxNumOfLogEntries + "</div>")
        } 
        if ($logEntries.length == 0) {
            if ($buildLog.children(".no-entries").length == 0)
                $buildLog.prepend("<div class='no-entries'>No log entries</div>");    
        } else {
            $buildLog.children(".no-entries").remove();
        }

        $buildLog.scrollTop($buildLog[0].scrollHeight);
    }

}
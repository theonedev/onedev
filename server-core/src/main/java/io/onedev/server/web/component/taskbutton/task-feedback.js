onedev.server.taskFeedback = {
	processData: function(containerId, callback, data) {
		const maxLogEntries = 2000;
		
		var $logEntries = $("#" + containerId).find(".task-log");
		for (const logEntry of data.logEntries)
			$logEntries.append(onedev.server.jobLogEntry.render(logEntry, false));
		
		const $children = $logEntries.children();
		const numOfLogEntriesToRemove = $children.length - maxLogEntries;
		if (numOfLogEntriesToRemove > 0) 
			$children.filter(":lt(" + numOfLogEntriesToRemove + ")").remove();			
		
		if (data.logEntries.length != 0) 
			$logEntries.scrollTop($logEntries[0].scrollHeight);			
			
		if (!data.finished) {
			setTimeout(function() {
				callback();
			}, 1000);
		}
	}
}
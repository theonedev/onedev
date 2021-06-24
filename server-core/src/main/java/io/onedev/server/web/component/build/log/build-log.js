onedev.server.buildLog = {
    appendLogEntries: function(containerId, logEntries, maxNumOfEntries) {
        var $buildLog = $("#" + containerId + ">.build-log");

        for (var i=0; i<logEntries.length; i++) {
			var logEntry = logEntries[i];
			var $logEntry = $("<div class='log-entry'></div>");
			$buildLog.append($logEntry);
			$logEntry.append("<span class='date mr-3'>" + moment(logEntry.date).format("HH:mm:ss") + "</span>");
			for (var j=0; j<logEntries[i].messages.length; j++) {	
				var message = logEntries[i].messages[j];
				var $message = $("<span class='message'></span>")
				if (message.style.bold)
					$message.addClass("font-weight-bold");
				if ((message.style.color == "37" || message.style.color == "97") 
						&& message.style.backgroundColor == "bg-default") {
					$message.addClass("text-black");
				} else {
					$message.addClass("fg-" + message.style.color);
					$message.addClass("bg-" + message.style.backgroundColor);
				}
				$message.text(message.text);
				$logEntry.append($message);
			}
			$buildLog.append($logEntry);
		}
        
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


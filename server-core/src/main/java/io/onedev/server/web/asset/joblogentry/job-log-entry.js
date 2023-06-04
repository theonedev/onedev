onedev.server.jobLogEntry = {
	
	render(logEntry, withDate) {
		var $logEntry = $("<div class='log-entry'></div>");
		if (withDate)
			$logEntry.append("<span class='date mr-2'>" + moment(logEntry.date).format("HH:mm:ss") + "</span> ");
		for (var i=0; i<logEntry.messages.length; i++) {	
			var message = logEntry.messages[i];
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
		return $logEntry;
	}
	
}
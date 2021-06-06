onedev.server.taskFeedback = {
	processData: function(containerId, callback, data) {
		const maxMessages = 2000;
		
		var $messages = $("#" + containerId).find(".messages");
		for (const message of data.messages) {
			var $message = $("<div class='message'></div>");
			$message.text(message);
			$messages.append($message);
		}
		
		const $children = $messages.children();
		const numOfMessagesToRemove = $children.length - maxMessages;
		if (numOfMessagesToRemove > 0) 
			$children.filter(":lt(" + numOfMessagesToRemove + ")").remove();			
		
		if (data.messages.length != 0) 
			$messages.scrollTop($messages[0].scrollHeight);			
			
		if (!data.finished) {
			setTimeout(function() {
				callback(data.lastMessageIndex);
			}, 1000);
		}
	}
}
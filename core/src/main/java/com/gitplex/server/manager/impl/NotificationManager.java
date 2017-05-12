package com.gitplex.server.manager.impl;

import com.gitplex.server.event.codecomment.CodeCommentEvent;

public interface NotificationManager {
	
	void sendNotifications(CodeCommentEvent event);
	
}

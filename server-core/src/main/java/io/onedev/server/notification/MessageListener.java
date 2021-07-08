package io.onedev.server.notification;

import javax.mail.Message;

public interface MessageListener {
	
	void onReceived(Message message);
	
}
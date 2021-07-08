package io.onedev.server.notification;

public interface InboxMonitor {

	void stop();
	
	void waitForFinish() throws InterruptedException;
	
}
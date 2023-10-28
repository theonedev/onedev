package io.onedev.server.mail;

import javax.mail.Message;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface InboxMonitor {
	
	Future<?> monitor(Consumer<Message> messageConsumer, boolean testMode);
	
	boolean isMonitorSystemAddressOnly();
	
}

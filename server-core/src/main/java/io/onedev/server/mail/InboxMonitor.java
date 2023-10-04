package io.onedev.server.mail;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface InboxMonitor {
	
	Future<?> monitor(Consumer<MailMessage> mailConsumer, boolean test);
	
}

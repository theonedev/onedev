package io.onedev.server.ee.sendgrid;

import javax.mail.Message;
import java.util.concurrent.SynchronousQueue;

class MessageTarget {
	
	private final String secret;
	
	private final SynchronousQueue<Message> queue = new SynchronousQueue<>();
	
	MessageTarget(String secret) {
		this.secret = secret;
	}

	String getSecret() {
		return secret;
	}

	SynchronousQueue<Message> getQueue() {
		return queue;
	}
}

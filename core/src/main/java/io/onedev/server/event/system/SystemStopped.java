package io.onedev.server.event.system;

import java.util.Date;

import io.onedev.server.event.Event;

public class SystemStopped extends Event {

	public SystemStopped() {
		super(null, new Date());
	}

}

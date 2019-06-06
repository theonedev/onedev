package io.onedev.server.event.system;

import java.util.Date;

import io.onedev.server.event.Event;

public class SystemStopping extends Event {

	public SystemStopping() {
		super(null, new Date());
	}

}

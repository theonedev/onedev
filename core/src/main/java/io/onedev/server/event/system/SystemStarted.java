package io.onedev.server.event.system;

import java.util.Date;

import io.onedev.server.event.Event;

public class SystemStarted extends Event {

	public SystemStarted() {
		super(null, new Date());
	}

}

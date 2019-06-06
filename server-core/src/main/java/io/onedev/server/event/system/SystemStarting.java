package io.onedev.server.event.system;

import java.util.Date;

import io.onedev.server.event.Event;

public class SystemStarting extends Event {

	public SystemStarting() {
		super(null, new Date());
	}

}

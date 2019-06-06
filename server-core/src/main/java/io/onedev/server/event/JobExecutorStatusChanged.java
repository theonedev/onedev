package io.onedev.server.event;

import java.util.Date;

public class JobExecutorStatusChanged extends Event {

	public JobExecutorStatusChanged() {
		super(null, new Date());
	}

}

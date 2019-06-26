package io.onedev.server.ci.job.log;

import javax.annotation.Nullable;

public abstract class JobLogger {

	public abstract void log(String message, @Nullable Throwable t);
	
	public void log(String message) {
		log(message, null);
	}

}

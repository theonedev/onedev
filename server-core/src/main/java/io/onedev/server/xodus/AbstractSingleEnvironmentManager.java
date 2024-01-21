package io.onedev.server.xodus;

import java.io.File;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStopping;
import jetbrains.exodus.env.Environment;

public abstract class AbstractSingleEnvironmentManager extends AbstractEnvironmentManager {
	
	private Environment env;

	protected synchronized Environment getEnv() {
		if (env == null) 
			env = newEnv(getEnvDir());
		return env;
	}

	@Listen
	public synchronized void on(SystemStopping event) {
		if (env != null)
			env.close();
	}
	
	protected abstract File getEnvDir();
	
}

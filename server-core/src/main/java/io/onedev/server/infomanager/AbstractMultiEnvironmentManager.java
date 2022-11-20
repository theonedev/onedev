package io.onedev.server.infomanager;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStopping;
import jetbrains.exodus.env.Environment;

public abstract class AbstractMultiEnvironmentManager extends AbstractEnvironmentManager {
	
	private final Map<String, Environment> envs = new ConcurrentHashMap<>();
	
	protected abstract File getEnvDir(String envKey);
	
	protected Environment getEnv(String envKey) {
		Environment env = envs.get(envKey);
		if (env == null) synchronized (envs) {
			env = envs.get(envKey);
			if (env == null) {
				env = newEnv(getEnvDir(envKey));
				envs.put(envKey, env);
			}
		}
		return env;
	}
	
	protected void removeEnv(String envKey) {
		synchronized (envs) {
			Environment env = envs.remove(envKey);
			if (env != null)
				env.close();
		}
	}

	@Listen
	public void on(SystemStopping event) {
		synchronized (envs) {
			for (Environment env: envs.values())
				env.close();
			envs.clear();
		}
	}

}

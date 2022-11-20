package io.onedev.server.buildspec.job.log.instruction;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class PauseExecution extends LogInstruction {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public PauseExecution(ListenerRegistry listenerRegistry) {
		this.listenerRegistry = listenerRegistry;
	}
	
	@Override
	public String getName() {
		return "PauseExecution";
	}

	@Transactional
	@Override
	public void execute(Build build, Map<String, List<String>> params, TaskLogger taskLogger) {
		build.setPaused(true);
		listenerRegistry.post(new BuildUpdated(build));
	}

}

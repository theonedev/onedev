package io.onedev.server.ci.job.log.instruction;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.event.build.BuildUpdated;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class SetBuildVersion extends LogInstruction {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public SetBuildVersion(ListenerRegistry listenerRegistry) {
		this.listenerRegistry = listenerRegistry;
	}
	
	@Override
	public String getName() {
		return "SetBuildVersion";
	}

	@Transactional
	@Override
	public void execute(Build build, Map<String, List<String>> params) {
		build.setVersion(params.values().iterator().next().iterator().next());
		listenerRegistry.post(new BuildUpdated(build));
	}

}

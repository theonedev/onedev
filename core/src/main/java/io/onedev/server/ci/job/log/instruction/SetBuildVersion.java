package io.onedev.server.ci.job.log.instruction;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.onedev.server.model.Build;

@Singleton
public class SetBuildVersion extends LogInstruction {

	@Override
	public String getName() {
		return "SetBuildVersion";
	}

	@Override
	public void execute(Build build, Map<String, List<String>> params) {
		build.setVersion(params.values().iterator().next().iterator().next());
	}

}

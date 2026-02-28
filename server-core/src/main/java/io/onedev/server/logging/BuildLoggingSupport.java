package io.onedev.server.logging;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.instruction.LogInstruction;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.model.Build;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;

public class BuildLoggingSupport implements LoggingSupport {

	private static final long serialVersionUID = 1L;
	
	private final BuildLoggingIdentity identity;

	private final Long buildId;

	private final Collection<String> maskSecrets;

	public BuildLoggingSupport(Build build) {
		identity = new BuildLoggingIdentity(build.getProject().getId(), build.getNumber());
		buildId = build.getId();
		maskSecrets = build.getMaskSecrets();
	}

	@Override
	public LoggingIdentity getIdentity() {
		return identity;
	}

	public Long getBuildId() {
		return buildId;
	}

	@Override
	public Collection<String> getMaskSecrets() {
		return maskSecrets;
	}

	@Override
	public String getChangeObservable() {
		return Build.getLogChangeObservable(buildId);
	}

	private ListenerRegistry getListenerRegistry() {
		return OneDev.getInstance(ListenerRegistry.class);
	}

	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	private Build getBuild() {
		return OneDev.getInstance(BuildService.class).load(buildId);
	}

	@Override
	public Collection<LogInstruction> getInstructions() {
		return Set.of(
			new LogInstruction() {
				
				@Override
				public String getName() {
					return "SetBuildVersion";
				}
				
				@Override
				public void execute(Map<String, List<String>> params) {
					String version = params.values().iterator().next().iterator().next();
					var build = getBuild();
					if (StringUtils.isNotBlank(version))
						build.setVersion(version);
					else
						build.setVersion(null);
					getListenerRegistry().post(new BuildUpdated(build));				
				}

			},
			new LogInstruction() {
				
				@Override
				public String getName() {
					return "PauseExecution";
				}
				
				@Override
				public void execute(Map<String, List<String>> params) {
					var build = getBuild();
					build.setPaused(true);
					getListenerRegistry().post(new BuildUpdated(build));
				}

			}
		);
	}

	@Override
	public <T> T runOnActiveServer(ClusterTask<T> task) {
		return getProjectService().runOnActiveServer(identity.getProjectId(), task);
	}

	@Override
	public void fileModified() {
		getProjectService().directoryModified(identity.getProjectId(), identity.getFile().getParentFile());
	}

	@Override
	public @Nullable Date getEffectiveDate() {
		return getBuild().getRetryDate();
	}
	
}
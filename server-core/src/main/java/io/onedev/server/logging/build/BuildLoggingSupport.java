package io.onedev.server.logging.build;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.logging.LoggingSupport;
import io.onedev.server.logging.LoggingIdentity;
import io.onedev.server.logging.instruction.LogInstruction;
import io.onedev.server.model.Build;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;

/** Logging support for one named build stage. */
public class BuildLoggingSupport implements LoggingSupport {

	private static final long serialVersionUID = 1L;
	
	private final Long projectId;

	private final Long buildNumber;

	private final Long buildId;

	private final Collection<String> maskSecrets;

	private final BuildLoggingIdentity identity;

	private final String stage;

	public BuildLoggingSupport(Build build, String stage) {
		this(build.getProject().getId(), build.getNumber(), build.getId(), build.getMaskSecrets(), stage);
	}

	/** Storage-only support, constructed without database access on the active server. */
	BuildLoggingSupport(BuildLogContext context, String stage) {
		this(context.getProjectId(), context.getBuildNumber(), context.getBuildId(), List.of(), stage);
	}

	private BuildLoggingSupport(Long projectId, Long buildNumber, Long buildId,
			Collection<String> maskSecrets, String stage) {
		this.projectId = projectId;
		this.buildNumber = buildNumber;
		this.buildId = buildId;
		this.maskSecrets = maskSecrets;
		this.stage = Objects.requireNonNull(stage);
		identity = new BuildLoggingIdentity(projectId, buildNumber, stage);
	}

	String getStage() {
		return stage;
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
		return getProjectService().runOnActiveServer(projectId, task);
	}

	@Override
	public void fileModified() {
		getProjectService().directoryModified(projectId, Build.getLogDir(projectId, buildNumber));
	}

	@Override
	public @Nullable Date getEffectiveDate() {
		return getBuild().getRetryDate();
	}
	
}

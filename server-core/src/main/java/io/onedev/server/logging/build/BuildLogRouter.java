package io.onedev.server.logging.build;

import static io.onedev.k8shelper.JobHelper.FINALIZATION;
import static io.onedev.k8shelper.JobHelper.INITIALIZATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;

import io.onedev.agent.job.JobUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.JobHelper;
import io.onedev.k8shelper.JobHelper.StepEvent;
import io.onedev.k8shelper.JobHelper.StepEventKind;
import io.onedev.server.OneDev;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.build.StepExecution;
import io.onedev.server.model.support.build.StepExecution.Status;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.BuildService;

/**
 * One router per build token. Executors finish a stage's output before sending the next boundary.
 * Build metadata and job-context reads precede the monitor. Flushes run during stage changes.
 * Neither log writers nor callbacks may acquire this monitor while holding the log lock.
 */
class BuildLogRouter extends TaskLogger {
	private final Long buildId;
	private final String token;
	private final BuildLogService buildLogService;
	// Routing state is guarded by this monitor. Never retain a Hibernate entity here.
	private TaskLogger currentLogger;
	private AtomicBoolean acceptingEntries;
	private Map<String, Integer> stepIndices = Map.of();
	private String stage;
	private String title;
	private boolean phaseAnnounced;
	private volatile long generation;
	private volatile boolean closed;
	private volatile BuildLoggingSupport currentSupport;

	BuildLogRouter(Build build, BuildLogService buildLogService) {
		buildId = build.getId();
		token = build.getToken();
		this.buildLogService = buildLogService;
		// Terminal status can be set before the final diagnostic message is logged.
		closed = build.getFinishDate() != null;
	}

	@Nullable
	private Build loadBuild() {
		var build = OneDev.getInstance(BuildService.class).get(buildId);
		if (closed || build == null || build.getFinishDate() != null || !Objects.equals(token, build.getToken())) {
			close();
			return null;
		}
		return build;
	}

	private void initializeStage(long expectedGeneration) {
		if (currentSupport != null || closed)
			return;
		OneDev.getInstance(TransactionService.class).run(() -> {
			var build = loadBuild();
			if (build == null)
				return;
			var support = new BuildLoggingSupport(build, build.getCurrentLogStage());
			var stageTitle = build.getLogStageName(support.getStage());
			synchronized (this) {
				if (!closed && generation == expectedGeneration && currentSupport == null) {
					selectStage(support, stageTitle);
					phaseAnnounced = build.isFinalization();
				}
			}
		});
	}

	private Map<String, Integer> getStepIndices() {
		synchronized (this) {
			if (!stepIndices.isEmpty())
				return stepIndices;
		}
		// This synchronous cluster call only reads the job server's in-memory context.
		var result = new HashMap<String, Integer>();
		var context = OneDev.getInstance(JobService.class).getJobContext(token, false);
		if (context != null) {
			new CompositeFacade(context.getActions()).traverse((facade, position) -> {
				result.put("step-" + JobHelper.stringifyStepPosition(position), result.size() + 1);
				return null;
			}, new ArrayList<>());
		}
		return Map.copyOf(result);
	}

	// Called under the monitor, with fully materialized support and title.
	private void selectStage(BuildLoggingSupport support, String stageTitle) {
		stage = support.getStage();
		title = stageTitle;
		var accepting = new AtomicBoolean(true);
		currentLogger = buildLogService.newLogger(support, accepting::get);
		acceptingEntries = accepting;
		currentSupport = support;
		phaseAnnounced = false;
	}

	private boolean complete(Build build, Status outcome) {
		var execution = build.getStepExecutions().get(stage);
		if (execution != null && execution.getStatus() == Status.RUNNING) {
			execution.complete(outcome);
			var message = outcome == Status.UNKNOWN ? "Step \"" + title + "\" status is unknown"
					: "Step \"" + title + "\" is " + outcome.name().toLowerCase(Locale.ROOT);
			currentLogger.log(switch (outcome) {
				case FAILED -> wrapWithAnsiError(message);
				case SUCCESSFUL -> wrapWithAnsiSuccess(message);
				default -> wrapWithAnsiNotice(message);
			});
			return true;
		}
		return false;
	}

	synchronized void close() {
		closed = true;
		if (acceptingEntries != null)
			acceptingEntries.set(false);
	}

	void finish() {
		BuildLoggingSupport support;
		synchronized (this) {
			close();
			support = currentSupport;
		}
		if (support != null)
			buildLogService.flush(support);
	}

	/** Cleanup runs on the active server; its caller may hold database locks. */
	synchronized void reset(Runnable cleanup) {
		generation++;
		if (acceptingEntries != null)
			acceptingEntries.set(false);
		if (currentSupport != null) {
			// Materialize the cached stage so cleanup also finds it.
			buildLogService.flush(currentSupport);
		}
		cleanup.run();
		currentSupport = null;
		currentLogger = null;
		stage = null;
		title = null;
		phaseAnnounced = false;
		stepIndices = Map.of();
	}

	@Override
	public void log(String message, String sessionId) {
		var expectedGeneration = generation;
		initializeStage(expectedGeneration);
		if (closed || generation != expectedGeneration)
			return;
		// Consume protocol markers before masking/truncation.
		var phase = JobUtils.parsePhaseMessage(message);
		var event = JobHelper.parseStepEventMessage(message);
		if (phase != null || event != null) {
			update(phase, event, expectedGeneration);
		} else {
			TaskLogger logger;
			synchronized (this) {
				logger = closed || generation != expectedGeneration ? null : currentLogger;
			}
			if (logger != null)
				logger.log(message, sessionId);
		}
	}

	private void update(String phase, StepEvent event, long expectedGeneration) {
		var name = phase != null ? phase : event.step();
		var starting = phase != null || event.kind() == StepEventKind.START || event.kind() == StepEventKind.SKIP;
		var indices = starting && name.startsWith("step-") ? getStepIndices() : Map.<String, Integer>of();
		OneDev.getInstance(TransactionService.class).run(() -> {
			var build = loadBuild();
			if (build == null)
				return;
			// Lazy database reads and possible cluster lookups must precede the monitor.
			var support = new BuildLoggingSupport(build, name);
			var stageTitle = build.getLogStageName(name);
			synchronized (this) {
				if (closed || generation != expectedGeneration)
					return;
				boolean changed;
				if (starting) {
					changed = start(build, support, stageTitle, event, indices);
				} else {
					// Late or duplicate outcomes must not produce contradictory notices.
					changed = name.equals(stage) && complete(build, Status.valueOf(event.kind().name()));
				}
				if (!changed)
					return;
			}
			OneDev.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
		});
	}

	// All inputs are prepared before acquiring the monitor. Generated notices cannot execute log instructions.
	private boolean start(Build build, BuildLoggingSupport support, String stageTitle, StepEvent event,
			Map<String, Integer> indices) {
		var name = support.getStage();
		var execution = build.getStepExecutions().get(name);
		if (event == null && name.equals(stage) && phaseAnnounced)
			return false;
		if (event != null && (FINALIZATION.equals(stage) || execution != null
				&& (event.kind() == StepEventKind.START || execution.getStatus() != Status.RUNNING)))
			return false;
		if (!name.equals(stage)) {
			complete(build, Status.UNKNOWN);
			// Invalidate before flushing to reject writers already waiting for the log lock.
			acceptingEntries.set(false);
			buildLogService.flush(currentSupport);
			selectStage(support, stageTitle);
		}
		if (name.equals(INITIALIZATION)) {
			build.getStepExecutions().clear();
			stepIndices = Map.of();
		}
		build.setFinalization(name.equals(FINALIZATION));
		if (event == null) {
			phaseAnnounced = true;
			currentLogger.log(wrapWithAnsiNotice(name.equals(INITIALIZATION) ? "Preparing job..." : "Cleaning up job..."));
		} else {
			if (execution == null) {
				execution = new StepExecution();
				build.getStepExecutions().put(name, execution);
			}
			stepIndices = indices;
			var index = indices.get(name);
			if (index != null)
				execution.setStepPosition(index, indices.size());
			if (event.kind() == StepEventKind.SKIP) {
				execution.skip();
				currentLogger.log(wrapWithAnsiNotice("Step \"" + title + "\" is skipped"));
			} else {
				currentLogger.log(wrapWithAnsiNotice("Running step \"" + title + "\"..."));
			}
		}
		return true;
	}
}

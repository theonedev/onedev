package io.onedev.server.logging.build;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;

import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.logging.LogEntry;
import io.onedev.server.logging.LogListener;
import io.onedev.server.logging.LogService;
import io.onedev.server.logging.LogSnippet;
import io.onedev.server.logging.LoggingIdentity;
import io.onedev.server.logging.LoggingSupport;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.service.BuildService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultBuildLogService implements BuildLogService, Serializable {

	@Inject
	private LogService logService;

	@Inject
	private SessionService sessionService;

	@Inject
	private BuildService buildService;

	// Retain the current stage until finish; losing a router must not orphan a cached snippet.
	private final Cache<String, BuildLogRouter> routers = CacheBuilder.newBuilder().build();

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(BuildLogService.class);
	}

	@Override
	public TaskLogger newLogger(LoggingSupport loggingSupport) {
		return logService.newLogger(loggingSupport);
	}

	@Override
	public TaskLogger newLogger(LoggingSupport loggingSupport, BooleanSupplier acceptingEntries) {
		return logService.newLogger(loggingSupport, acceptingEntries);
	}

	@Override
	public List<LogEntry> readLogEntries(LoggingSupport loggingSupport, int offset, int count) {
		return logService.readLogEntries(loggingSupport, offset, count);
	}

	@Override
	public List<LogEntry> readLogEntries(LoggingIdentity identity, int offset, int count) {
		return logService.readLogEntries(identity, offset, count);
	}

	@Override
	public void registerListener(LogListener listener) {
		logService.registerListener(listener);
	}

	@Override
	public void deregisterListener(LogListener listener) {
		logService.deregisterListener(listener);
	}

	@Override
	public boolean matches(LoggingSupport loggingSupport, Pattern pattern) {
		return logService.matches(loggingSupport, pattern);
	}

	@Override
	public void flush(LoggingSupport loggingSupport) {
		logService.flush(loggingSupport);
	}

	@Override
	public void clear(LoggingSupport loggingSupport) {
		logService.clear(loggingSupport);
	}

	@Override
	public LogSnippet readLogSnippetReversely(LoggingSupport loggingSupport, int count) {
		return logService.readLogSnippetReversely(loggingSupport, count);
	}

	@Override
	public LogSnippet readLogSnippetReversely(LoggingIdentity identity, int count) {
		return logService.readLogSnippetReversely(identity, count);
	}

	@Override
	public InputStream openLogStream(LoggingIdentity loggingIdentity) {
		return logService.openLogStream(loggingIdentity);
	}

	@Override
	public @Nullable TaskLogger getLogger(String token) {
		return logService.getLogger(token);
	}

	@Override
	public void addLogger(String token, TaskLogger logger) {
		logService.addLogger(token, logger);
	}

	@Override
	public void removeLogger(String token) {
		logService.removeLogger(token);
	}

	@Override
	public TaskLogger newLogger(Build build) {
		return build.getFinishDate() == null ? getRouter(build) : new BuildLogRouter(build, this);
	}

	private BuildLogRouter getRouter(Build build) {
		try {
			return routers.get(build.getToken(), () -> new BuildLogRouter(build, this));
		} catch (ExecutionException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	private List<BuildLoggingSupport> getStageSupports(BuildLogContext context) {
		return sessionService.call(() -> {
			var build = buildService.load(context.getBuildId());
			return build.getLogStages().stream().map(it -> new BuildLoggingSupport(build, it)).toList();
		});
	}

	@Override
	public boolean matches(BuildLogContext context, Pattern pattern) {
		return getStageSupports(context).stream().anyMatch(it -> logService.matches(it, pattern));
	}

	@Override
	public void finish(Build build) {
		var router = routers.getIfPresent(build.getToken());
		if (router != null) {
			router.finish();
			routers.asMap().remove(build.getToken(), router);
		}
	}

	@Override
	public InputStream openLogStream(List<? extends LoggingIdentity> stages) {
		return new BuildLogStream(stages, this);
	}

	private record SnapshotMetadata(String logVersion, List<BuildLoggingSupport> stages) implements Serializable {
	}

	@Override
	public BuildLogSnapshot readSnapshot(BuildLogContext context,
			BuildLogSnapshot previous, int tailCount) {
		var previousOffsets = previous != null ? previous.offsets : null;
		var previousVersion = previous != null ? previous.logVersion : null;
		while (true) {
			// Read database metadata on the caller, which may already hold database locks.
			// The active-server task must not wait for those locks while the caller waits for it.
			var metadata = OneDev.getInstance(SessionService.class).call(() ->
					new SnapshotMetadata(context.getLogVersion(), getStageSupports(context)));
			var snapshot = context.runOnActiveServer(new ClusterTask<BuildLogSnapshot>() {

				private static final long serialVersionUID = 1L;

				@Override
				public BuildLogSnapshot call() {
					// Keep entries and their offsets together under the shared build lock.
					return read(context.getStorage().getLockName(), () -> {
						var result = new BuildLogSnapshot();
						result.logVersion = metadata.logVersion();
						var offsets = Objects.equals(previousVersion, result.logVersion) ? previousOffsets : null;
						for (var stage : metadata.stages()) {
							if (offsets == null) {
								var snippet = logService.readLogSnippetReversely(stage.getIdentity(), tailCount);
								result.offsets.put(stage.getStage(), snippet.offset + snippet.entries.size());
								result.entries.addAll(snippet.entries);
							} else {
								int offset = offsets.getOrDefault(stage.getStage(), 0);
								var entries = logService.readLogEntries(stage.getIdentity(), offset, 0);
								result.offsets.put(stage.getStage(), offset + entries.size());
								result.entries.addAll(entries);
							}
							if (offsets == null) {
								result.entries.sort(Comparator.comparing(LogEntry::getDate));
								if (result.entries.size() > tailCount)
									result.entries.subList(0, result.entries.size() - tailCount).clear();
							}
						}
						// Merge concurrent stage output by entry time, retaining stage order for equal timestamps.
						if (offsets != null)
							result.entries.sort(Comparator.comparing(LogEntry::getDate));
						return result;
					});
				}

			});
			// Check for an attempt change on the caller after the task releases the log lock.
			if (Objects.equals(metadata.logVersion(), context.getLogVersion()))
				return snapshot;
		}
	}

	@Override
	public void clear(BuildLogContext context) {
		context.runOnActiveServer(new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() {
				var router = routers.getIfPresent(context.getToken());
				if (router != null)
					router.reset(() -> clearStages(context));
				else
					clearStages(context);
				context.fileModified();
				return null;
			}
		});
	}

	private void clearStages(BuildLogContext context) {
		write(context.getStorage().getLockName(), () -> {
			var directory = context.getStorage().getDirectory();
			var files = directory.listFiles();
			if (files != null) {
				for (var file : files) {
					var name = file.getName();
					if (file.isFile() && name.endsWith(".log")) {
						var stage = name.substring(0, name.length() - 4);
						logService.clear(new BuildLoggingSupport(context, stage) {

							@Override
							public void fileModified() {
							}

						});
					}
				}
			}
			return null;
		});
	}

}

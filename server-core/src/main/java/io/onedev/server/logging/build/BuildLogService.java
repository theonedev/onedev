package io.onedev.server.logging.build;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.logging.LogService;
import io.onedev.server.logging.LoggingIdentity;
import io.onedev.server.model.Build;

/** Build stage routing, aggregation, and retry lifecycle, backed by {@link LogService}. */
public interface BuildLogService extends LogService {

	TaskLogger newLogger(Build build);

	boolean matches(BuildLogContext context, Pattern pattern);

	/** Stop accepting output and flush the current stage on the build's active server. */
	void finish(Build build);

	/** Read a combined build tail initially or after a retry, otherwise all new stage entries. */
	BuildLogSnapshot readSnapshot(BuildLogContext context, @Nullable BuildLogSnapshot previous, int tailCount);

	/** Concatenate stage files locally under their shared lock. Read and close on the opening thread. */
	InputStream openLogStream(List<? extends LoggingIdentity> stages);

	/** Discard persisted and cached logs of the previous build attempt. */
	void clear(BuildLogContext context);

}

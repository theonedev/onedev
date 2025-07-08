package io.onedev.server.rest.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.job.log.LogListener;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.job.log.LogSnippet;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Api(description="Build log stream resource is operated with build id, which is different from build number. "
		+ "To get build id of a particular build number, use the <a href='/~help/api/io.onedev.server.rest.BuildResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;path/to/project#100&quot;</code> or <code>&quot;Number&quot; is &quot;PROJECTKEY-100&quot;</code>")
@Path("/streaming/build-logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class BuildLogStreamResource {

	private static final int MAX_LOG_ENTRIES = 1000;
	
	private final BuildManager buildManager;
	
	private final LogManager logManager;
	
	private final ObjectMapper objectMapper;
	
	private final SessionManager sessionManager;
	
	@Inject
	public BuildLogStreamResource(BuildManager buildManager, LogManager logManager,
								  ObjectMapper objectMapper, SessionManager sessionManager) {
		this.buildManager = buildManager;
		this.logManager = logManager;
		this.objectMapper = objectMapper;
		this.sessionManager = sessionManager;
	}
	
	@Api(order=200, description = "Streaming log of specified build")
	@Path("/{buildId}")
	@GET
	@Produces(APPLICATION_OCTET_STREAM)
	public StreamingOutput downloadLog(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
		if (!SecurityUtils.canAccessLog(build))
			throw new UnauthorizedException();
		
		var projectId = build.getProject().getId();
		var buildNumber = build.getNumber();
		var buildStatus = build.getStatus();

		return os -> {
			writeStatus(os, buildStatus);
			var logListener = new LogListener() {

				@Override
				public void logged(Long loggedBuildId) {
					if (loggedBuildId.equals(buildId)) {
						synchronized (os) {
							os.notify();
						}
					}
				}

			};
			logManager.registerListener(logListener);
			sessionManager.closeSession();
			try {
				var nextOffset = 0;
				LogSnippet snippet = logManager.readLogSnippetReversely(projectId, buildNumber, MAX_LOG_ENTRIES + 1);
				nextOffset = snippet.offset + snippet.entries.size();
				for (var entry : snippet.entries)
					writeEntry(os, entry);

				while (true) {
					synchronized (os) {
						try {
							os.wait(5000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						var entries = logManager.readLogEntries(projectId, buildNumber, nextOffset, 0);
						if (!entries.isEmpty()) {
							nextOffset += entries.size();
							for (var entry : entries)
								writeEntry(os, entry);
						} else {
							var innerBuildStatus = sessionManager.call(() -> buildManager.load(buildId).getStatus());
							if (innerBuildStatus.isFinished()) {
								writeStatus(os, innerBuildStatus);
								break;
							} else {
								writeInt(os, 0);
								os.flush();
							}
						}
					}
				}
			} finally {
				sessionManager.openSession();
				logManager.deregisterListener(logListener);
			}
		};
	}
	
	private void writeInt(OutputStream os, int value) {
		try {
			os.write(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeStatus(OutputStream os, Status status) {
		try {
			writeInt(os, status.name().length() * -1);
			os.write(status.name().getBytes(UTF_8));
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeEntry(OutputStream os, JobLogEntryEx entry) {
		try {
			var bytes = objectMapper.writeValueAsBytes(entry.transformEmojis());
			writeInt(os, bytes.length);
			os.write(bytes);
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}

package io.onedev.server.rest.resource;

import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.shiro.authz.UnauthorizedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.logging.LogEntry;
import io.onedev.server.logging.LogListener;
import io.onedev.server.logging.LoggingSupport;
import io.onedev.server.logging.build.BuildLogService;
import io.onedev.server.logging.build.BuildLoggingSupport;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Api(description="Build log stream resource is operated with build id, which is different from build number. "
		+ "To get build id of a particular build number, use the <a href='/~help/api/io.onedev.server.rest.BuildResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;path/to/project#100&quot;</code> or <code>&quot;Number&quot; is &quot;PROJECTKEY-100&quot;</code>")
@Path("/streaming/build-logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class BuildLogStreamResource {

	private static final int MAX_LOG_ENTRIES = 1000;
	
	private final BuildService buildService;
	
	private final BuildLogService buildLogService;
	
	private final ObjectMapper objectMapper;
	
	private final SessionService sessionService;
	
	@Inject
	public BuildLogStreamResource(BuildService buildService, BuildLogService buildLogService,
                                  ObjectMapper objectMapper, SessionService sessionService) {
		this.buildService = buildService;
		this.buildLogService = buildLogService;
		this.objectMapper = objectMapper;
		this.sessionService = sessionService;
	}
	
	@Api(order=200, description = "Streaming log of specified build")
	@Path("/{buildId}")
	@GET
	@Produces(APPLICATION_OCTET_STREAM)
	public StreamingOutput downloadLog(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canAccessLog(build))
			throw new UnauthorizedException();
		
		var logContext = build.getLogContext();
		var buildStatus = build.getStatus();

		return os -> {
			writeStatus(os, buildStatus);
			var logListener = new LogListener() {

				@Override
				public void logged(LoggingSupport support) {
					if (support instanceof BuildLoggingSupport buildLoggingSupport 
							&& buildLoggingSupport.getBuildId().equals(buildId)) {
						synchronized (os) {
							os.notify();
						}
					}
				}

			};
			buildLogService.registerListener(logListener);
			sessionService.closeSession();
			try {
				var snapshot = buildLogService.readSnapshot(logContext, null, MAX_LOG_ENTRIES + 1);
				for (var entry : snapshot.entries)
					writeEntry(os, entry);

				while (true) {
					synchronized (os) {
						try {
							os.wait(5000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						snapshot = buildLogService.readSnapshot(logContext, snapshot, MAX_LOG_ENTRIES + 1);
						if (!snapshot.entries.isEmpty()) {
							for (var entry : snapshot.entries)
								writeEntry(os, entry);
						} else {
							var innerBuildStatus = sessionService.call(() -> buildService.load(buildId).getStatus());
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
				sessionService.openSession();
				buildLogService.deregisterListener(logListener);
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
	
	private void writeEntry(OutputStream os, LogEntry entry) {
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

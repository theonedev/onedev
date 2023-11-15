package io.onedev.server.pack;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.job.JobManager;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class PackServlet extends HttpServlet {
	
	protected final JobManager jobManager;
	
	protected final BuildManager buildManager;
	
	protected final ObjectMapper objectMapper;
	
	public PackServlet(JobManager jobManager, BuildManager buildManager, ObjectMapper objectMapper) {
		this.jobManager = jobManager;
		this.buildManager = buildManager;
		this.objectMapper = objectMapper;
	}

	protected String getPossibleJobToken(HttpServletRequest req) {
		var auth = req.getHeader("Authorization");
		if (auth != null && auth.startsWith("Basic ")) {
			var basicAuth = auth.substring("Basic ".length());
			basicAuth = new String(Base64.decodeBase64(basicAuth), UTF_8);
			return StringUtils.substringBefore(basicAuth, ":");
		} else {
			return UUID.randomUUID().toString();
		}
	}
	
	protected void sendResponse(HttpServletResponse response, int statusCode, Object jsonObj) {
		response.setStatus(statusCode);
		try (var out = response.getOutputStream()) {
			out.write(objectMapper.writeValueAsString(jsonObj).getBytes(UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

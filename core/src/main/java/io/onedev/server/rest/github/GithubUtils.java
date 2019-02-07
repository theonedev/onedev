package io.onedev.server.rest.github;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.onedev.server.rest.RestConstants;

public class GithubUtils {
	
	public static Response buildErrorResponse(Status status, String message) {
		Map<String, String> entity = new HashMap<>();
		entity.put("error", message);
		entity.put("message", message);
		return Response.status(status).entity(entity).type(RestConstants.JSON_UTF8).build();
	}
	
}

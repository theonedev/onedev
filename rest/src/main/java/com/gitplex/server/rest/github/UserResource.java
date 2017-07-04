package com.gitplex.server.rest.github;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.rest.RestConstants;
import com.gitplex.server.security.SecurityUtils;

@Path("/user")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
@Singleton
public class UserResource {

	private final UserManager userManager;
	
	@Inject
	public UserResource(UserManager userManager) {
		this.userManager = userManager;
	}
	
    @GET
    public Response get(@Context UriInfo uriInfo) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> entity = new HashMap<>();
		
		User user = userManager.getCurrent();
		entity.put("name", user.getFullName());
		entity.put("login", user.getName());
		entity.put("id", "1000000");
		entity.put("type", "User");
		entity.put("url", uriInfo.getBaseUri().toString() + "/users/" + user.getName());
		entity.put("site_admin", SecurityUtils.isAdministrator());
		entity.put("created_at", new SimpleDateFormat(RestConstants.DATE_FORMAT).format(new Date()));
    	
		return Response.ok(entity, RestConstants.JSON_UTF8).build();
    }

}

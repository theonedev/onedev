package com.gitplex.server.rest.github;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.rest.RestConstants;
import com.gitplex.server.security.SecurityUtils;

@Path("/repos/projects")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
@Singleton
public class RepositoryResource {

	private final ProjectManager projectManager;
	
	@Inject
	public RepositoryResource(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}
	
	@Path("/{name}")
    @GET
    public Response get(@PathParam("name") String name) {
    	Project project = projectManager.find(name);
    	
    	if (!SecurityUtils.canRead(project)) {
			throw new UnauthorizedException("Unauthorized access to project " + project.getName());
    	} else {
    		Map<String, Object> entity = new HashMap<>();
    		Map<String, String> permissionsMap = new HashMap<>();
    		permissionsMap.put("admin", String.valueOf(SecurityUtils.canManage(project)));
    		permissionsMap.put("push", String.valueOf(SecurityUtils.canWrite(project)));
    		permissionsMap.put("pull", "true");
    		entity.put("permissions", permissionsMap);
    		entity.put("name", project.getName());
    		return Response.ok(entity, RestConstants.JSON_UTF8).build();
    	}
    }

}

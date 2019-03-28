package io.onedev.server.rest.github;

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
import javax.ws.rs.core.Response.Status;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.rest.RestConstants;
import io.onedev.server.security.SecurityUtils;

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
		if (project == null)
			return GithubUtils.buildErrorResponse(Status.NOT_FOUND, "Unable to find project '" + name + "'");
			
		if (!SecurityUtils.canReadCode(project.getFacade()))
    		return GithubUtils.buildErrorResponse(Status.FORBIDDEN, "Unauthorized access to project '" + name + "'");
    	
		Map<String, Object> entity = new HashMap<>();
		Map<String, String> permissionsMap = new HashMap<>();
		entity.put("name", project.getName());
		permissionsMap.put("admin", String.valueOf(SecurityUtils.canAdministrate(project.getFacade())));
		permissionsMap.put("push", String.valueOf(SecurityUtils.canWriteCode(project.getFacade())));
		permissionsMap.put("pull", "true");
		entity.put("permissions", permissionsMap);
		
		Map<String, String> ownerMap = new HashMap<>();
		ownerMap.put("login", "projects");
		ownerMap.put("id", "1000000");
		
		entity.put("owner", ownerMap);
		
		return Response.ok(entity, RestConstants.JSON_UTF8).build();
    }

}

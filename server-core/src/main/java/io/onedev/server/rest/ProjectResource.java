package io.onedev.server.rest;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.jersey.ValidQueryParams;
import io.onedev.server.security.SecurityUtils;

@Path("/projects")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ProjectResource {

	private final ProjectManager projectManager;
	
	@Inject
	public ProjectResource(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}
	
	@ValidQueryParams
	@GET
    public Response query(@QueryParam("name") String projectName, @QueryParam("offset") Integer offset, 
    		@QueryParam("count") Integer count, @Context UriInfo uriInfo) {
		EntityCriteria<Project> criteria = projectManager.newCriteria();
		if (projectName != null)
			criteria.add(Restrictions.eq("name", projectName));
		
    	if (offset == null)
    		offset = 0;
    	
    	if (count == null || count > RestConstants.PAGE_SIZE) 
    		count = RestConstants.PAGE_SIZE;

    	Collection<Project> projects = projectManager.query(criteria, offset, count);
		for (Project project: projects) {
			if (!SecurityUtils.canAccess(project))
				throw new UnauthorizedException("Unable to access project '" + project.getName() + "'");
		}
		
		return Response.ok(projects, RestConstants.JSON_UTF8).build();
    }
    
	@Path("/{projectId}")
    @GET
    public Project get(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canAccess(project))
			throw new UnauthorizedException("Unauthorized access to project " + project.getName());
    	else
    		return project;
    }
	
}

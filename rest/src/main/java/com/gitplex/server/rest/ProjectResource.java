package com.gitplex.server.rest;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.rest.jersey.ValidQueryParams;
import com.gitplex.server.security.SecurityUtils;

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
    public Collection<Project> query(@QueryParam("name") String projectName) {
		EntityCriteria<Project> criteria = projectManager.newCriteria();
		if (projectName != null)
			criteria.add(Restrictions.eq("name", projectName));
		
		Collection<Project> projects = new ArrayList<>();
		
		for (Project project: projectManager.findAll(criteria)) {
	    	if (SecurityUtils.canRead(project))
	    		projects.add(project);
		}
		
		return projects;
    }
    
	@Path("/{projectId}")
    @GET
    public Project get(@PathParam("projectId") Long projectId) {
    	Project project = projectManager.load(projectId);
    	if (!SecurityUtils.canRead(project))
			throw new UnauthorizedException("Unauthorized access to project " + project.getName());
    	else
    		return project;
    }
	
}

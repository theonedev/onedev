package com.gitplex.server.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.rest.jersey.ValidQueryParams;
import com.gitplex.server.security.SecurityUtils;

@Path("/projects")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ProjectResource {

	private final Dao dao;
	
	private final ProjectManager projectManager;
	
	@Inject
	public ProjectResource(Dao dao, ProjectManager projectManager) {
		this.dao = dao;
		this.projectManager = projectManager;
	}
	
	@Path("/{id}")
    @GET
    public Project get(@PathParam("id") Long id) {
    	Project project = dao.load(Project.class, id);

    	if (!SecurityUtils.canRead(project))
    		throw new UnauthenticatedException();
    	else
    		return project;
    }
    
	@ValidQueryParams
	@GET
	public Collection<Project> query(@QueryParam("userId") Long userId, @QueryParam("name") String name, 
			@QueryParam("path") String path) {
		
		List<Project> projects = new ArrayList<>();
		
		EntityCriteria<Project> criteria = EntityCriteria.of(Project.class);
		if (path != null) {
			Project project = projectManager.find(path);
			if (project != null)
				projects.add(project);
		} else {
			if (userId != null)
				criteria.add(Restrictions.eq("user.id", userId));
			if (name != null)
				criteria.add(Restrictions.eq("name", name));
			projects.addAll(dao.findAll(criteria));
		}
		
		for (Project project: projects) {
			if (!SecurityUtils.canRead(project)) 
				throw new UnauthorizedException("Unauthorized access to project " + project.getName());
		}
		return projects;
	}

}

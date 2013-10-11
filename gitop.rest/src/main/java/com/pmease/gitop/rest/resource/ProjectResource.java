package com.pmease.gitop.rest.resource;

import io.dropwizard.jersey.params.LongParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
public class ProjectResource {

	private final ProjectManager projectManager;
	
	@Inject
	public ProjectResource(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}
	
	@Path("/{projectId}")
    @GET
    @Timed
    public Project get(@PathParam("projectId") LongParam projectId) {
    	Project project = projectManager.load(projectId.get());
    	project.getName();
    	project.getOwner().getRepositories().size();
    	return project;
    }
    
    @POST
    public Long save(Project project) {
    	projectManager.save(project);
    	return project.getId();
    }

}

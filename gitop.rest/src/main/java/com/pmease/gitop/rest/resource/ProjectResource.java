package com.pmease.gitop.rest.resource;

import io.dropwizard.jersey.params.LongParam;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.model.Repository;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
public class ProjectResource {

	private final RepositoryManager projectManager;
	
	@Inject
	public ProjectResource(RepositoryManager projectManager) {
		this.projectManager = projectManager;
	}
	
	@Path("/{projectId}")
    @GET
    public Repository get(@PathParam("projectId") LongParam projectId) {
    	return projectManager.load(projectId.get());
    }
    
    @POST
    public Long save(@Valid Repository project) {
    	projectManager.save(project);
    	return project.getId();
    }

}

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

@Path("/repositories")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryResource {

	private final RepositoryManager repositoryManager;
	
	@Inject
	public RepositoryResource(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	@Path("/{repositoryId}")
    @GET
    public Repository get(@PathParam("repositoryId") LongParam repositoryId) {
    	return repositoryManager.load(repositoryId.get());
    }
    
    @POST
    public Long save(@Valid Repository repository) {
    	repositoryManager.save(repository);
    	return repository.getId();
    }

}

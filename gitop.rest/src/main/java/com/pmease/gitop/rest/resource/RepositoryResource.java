package com.pmease.gitop.rest.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/repositories")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RepositoryResource {

	private final RepositoryManager repositoryManager;
	
	@Inject
	public RepositoryResource(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	@Path("/{repositoryId}")
    @GET
    public Repository get(@PathParam("repositoryId") Long repositoryId) {
    	Repository repository = repositoryManager.load(repositoryId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(repository)))
    		throw new UnauthenticatedException();
    	else
    		return repository;
    }
    
	@GET
	public Collection<Repository> query(
			@QueryParam("userId") Long userId, 
			@QueryParam("name") String name) {
		List<Criterion> criterions = new ArrayList<>();
		if (userId != null)
			criterions.add(Restrictions.eq("owner.id", userId));
		if (name != null)
			criterions.add(Restrictions.eq("name", name));
		List<Repository> repositories = repositoryManager.query(criterions.toArray(new Criterion[criterions.size()]));
		
		for (Repository repository: repositories) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(repository))) {
				throw new UnauthorizedException("Unauthorized access to repository " + repository.getPathName());
			}
		}
		return repositories;
	}

	@POST
    public Long save(@NotNull @Valid Repository repository) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(repository)))
    		throw new UnauthenticatedException();
    	repositoryManager.save(repository);
    	return repository.getId();
    }

    @DELETE
    @Path("/{repositoryId}")
    public void delete(@PathParam("repositoryId") Long repositoryId) {
    	Repository repository = repositoryManager.load(repositoryId);

    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(repository)))
    		throw new UnauthorizedException();
    	
    	repositoryManager.delete(repository);
    }
}

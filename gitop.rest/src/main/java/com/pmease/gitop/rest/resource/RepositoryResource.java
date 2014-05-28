package com.pmease.gitop.rest.resource;

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
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.jersey.ValidQueryParams;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/repositories")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RepositoryResource {

	private final Dao dao;
	
	@Inject
	public RepositoryResource(Dao dao) {
		this.dao = dao;
	}
	
	@Path("/{id}")
    @GET
    public Repository get(@PathParam("id") Long id) {
    	Repository repository = dao.load(Repository.class, id);

    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryRead(repository)))
    		throw new UnauthenticatedException();
    	else
    		return repository;
    }
    
	@ValidQueryParams
	@GET
	public Collection<Repository> query(
			@QueryParam("user") Long userId, 
			@QueryParam("name") String name) {
		EntityCriteria<Repository> criteria = EntityCriteria.of(Repository.class);
		if (userId != null)
			criteria.add(Restrictions.eq("owner.id", userId));
		if (name != null)
			criteria.add(Restrictions.eq("name", name));
		List<Repository> repositories = dao.query(criteria);
		
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
    		throw new UnauthorizedException();
    	
    	dao.persist(repository);
    	return repository.getId();
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
    	Repository repository = dao.load(Repository.class, id);

    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(repository)))
    		throw new UnauthorizedException();
    	
    	dao.remove(repository);
    }
    
}

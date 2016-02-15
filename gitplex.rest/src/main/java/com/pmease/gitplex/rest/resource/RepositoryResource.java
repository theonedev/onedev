package com.pmease.gitplex.rest.resource;

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
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.jersey.ValidQueryParams;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.permission.ObjectPermission;

@Path("/repositories")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RepositoryResource {

	private final Dao dao;
	
	private final DepotManager depotManager;
	
	@Inject
	public RepositoryResource(Dao dao, DepotManager depotManager) {
		this.dao = dao;
		this.depotManager = depotManager;
	}
	
	@Path("/{id}")
    @GET
    public Depot get(@PathParam("id") Long id) {
    	Depot depot = dao.load(Depot.class, id);

    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotPull(depot)))
    		throw new UnauthenticatedException();
    	else
    		return depot;
    }
    
	@ValidQueryParams
	@GET
	public Collection<Depot> query(@QueryParam("userId") Long userId, @QueryParam("name") String name, 
			@QueryParam("path") String path) {
		
		List<Depot> depots = new ArrayList<>();
		
		EntityCriteria<Depot> criteria = EntityCriteria.of(Depot.class);
		if (path != null) {
			Depot depot = depotManager.findBy(path);
			if (depot != null)
				depots.add(depot);
		} else {
			if (userId != null)
				criteria.add(Restrictions.eq("owner.id", userId));
			if (name != null)
				criteria.add(Restrictions.eq("name", name));
			depots.addAll(dao.query(criteria));
		}
		
		for (Depot depot: depots) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotPull(depot))) 
				throw new UnauthorizedException("Unauthorized access to repository " + depot.getFQN());
		}
		return depots;
	}

	@POST
    public Long save(@NotNull @Valid Depot depot) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot)))
    		throw new UnauthorizedException();
    	
    	dao.persist(depot);
    	return depot.getId();
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
    	Depot depot = dao.load(Depot.class, id);

    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot)))
    		throw new UnauthorizedException();
    	
    	dao.remove(depot);
    }
    
}

package com.gitplex.server.rest;

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
import org.hibernate.validator.constraints.Email;

import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.rest.jersey.ValidQueryParams;
import com.gitplex.server.security.SecurityUtils;

@Path("/users")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserResource {

	private final UserManager userManager;
	
	@Inject
	public UserResource(UserManager userManager) {
		this.userManager = userManager;
	}
	
	@ValidQueryParams
	@GET
	public Collection<User> query(@QueryParam("name") String name, @Email @QueryParam("email") String email) {
    	if (!SecurityUtils.canAccessPublic())
    		throw new UnauthorizedException("Unauthorized access to user profiles");
    	EntityCriteria<User> criteria = EntityCriteria.of(User.class);
    	if (name != null)
    		criteria.add(Restrictions.eq("name", name));
		if (email != null)
			criteria.add(Restrictions.eq("email", email));
		return userManager.findAll(criteria);
	}
	
    @GET
    @Path("/{userId}")
    public User get(@PathParam("userId") Long userId) {
    	if (!SecurityUtils.canAccessPublic())
    		throw new UnauthorizedException("Unauthorized access to user profile");
    	return userManager.load(userId);
    }
    
}

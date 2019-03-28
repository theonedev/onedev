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
import org.hibernate.validator.constraints.Email;

import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.rest.jersey.ValidQueryParams;
import io.onedev.server.security.SecurityUtils;

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
	public Response query(@QueryParam("name") String name, @Email @QueryParam("email") String email, 
			@QueryParam("offset") Integer offset, @QueryParam("count") Integer count, @Context UriInfo uriInfo) {
    	if (!SecurityUtils.isAdministrator())
    		throw new UnauthorizedException("Unauthorized access to user profiles");
    	
    	EntityCriteria<User> criteria = EntityCriteria.of(User.class);
    	if (name != null)
    		criteria.add(Restrictions.eq("name", name));
		if (email != null)
			criteria.add(Restrictions.eq("email", email));
		
    	if (offset == null)
    		offset = 0;
    	
    	if (count == null || count > RestConstants.PAGE_SIZE) 
    		count = RestConstants.PAGE_SIZE;

    	Collection<User> users = userManager.query(criteria, offset, count);
		
		return Response.ok(users, RestConstants.JSON_UTF8).build();
	}
	
    @GET
    @Path("/{userId}")
    public User get(@PathParam("userId") Long userId) {
    	return userManager.load(userId);
    }
    
}

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
	public Response query(@QueryParam("name") String name, @Email @QueryParam("email") String email, 
			@QueryParam("per_page") Integer perPage, @QueryParam("page") Integer page, @Context UriInfo uriInfo) {
    	if (!SecurityUtils.canAccessPublic())
    		throw new UnauthorizedException("Unauthorized access to user profiles");
    	EntityCriteria<User> criteria = EntityCriteria.of(User.class);
    	if (name != null)
    		criteria.add(Restrictions.eq("name", name));
		if (email != null)
			criteria.add(Restrictions.eq("email", email));
		
    	if (page == null)
    		page = 1;
    	
    	if (perPage == null || perPage > RestConstants.PAGE_SIZE) 
    		perPage = RestConstants.PAGE_SIZE;

    	int totalCount = userManager.count(criteria);

    	Collection<User> users = userManager.findRange(criteria, (page-1)*perPage, perPage);
		
		return Response
				.ok(users, RestConstants.JSON_UTF8)
				.links(PageUtils.getNavLinks(uriInfo, totalCount, perPage, page))
				.build();
	}
	
    @GET
    @Path("/{userId}")
    public User get(@PathParam("userId") Long userId) {
    	if (!SecurityUtils.canAccessPublic())
    		throw new UnauthorizedException("Unauthorized access to user profile");
    	return userManager.load(userId);
    }
    
}

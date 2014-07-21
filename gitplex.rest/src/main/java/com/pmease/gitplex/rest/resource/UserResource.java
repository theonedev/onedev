package com.pmease.gitplex.rest.resource;

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
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.validator.constraints.Email;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.jersey.ValidQueryParams;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;

@Path("/users")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class UserResource {

	private final Dao dao;
	
	@Inject
	public UserResource(Dao dao) {
		this.dao = dao;
	}
	
	@ValidQueryParams
	@GET
	public Collection<User> query(
			@QueryParam("name") String name, 
			@Email @QueryParam("email") String email, 
			@QueryParam("fullName") String fullName) {
    	EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		if (name != null)
			criteria.add(Restrictions.eq("name", name));
		if (email != null)
			criteria.add(Restrictions.eq("email", email));
		if (fullName != null)
			criteria.add(Restrictions.eq("fullName", fullName));
		List<User> users = dao.query(criteria);
		
		for (User user: users) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserRead(user))) {
				throw new UnauthorizedException("Unauthorized access to user " + user.getDisplayName());
			}
		}
		return users;
	}
	
    @GET
    @Path("/{id}")
    public User get(@PathParam("id") Long id) {
    	User user = dao.load(User.class, id);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserRead(user)))
    		throw new UnauthorizedException();
    	else
    		return user;
    }
    
    @POST
    public Long save(@NotNull @Valid User user) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(user)))
    		throw new UnauthorizedException();

    	dao.persist(user);
    	return user.getId();
    }
    
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
    	User user = dao.load(User.class, id);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(user)))
    		throw new UnauthorizedException();
    	
    	dao.remove(user);
    }
}

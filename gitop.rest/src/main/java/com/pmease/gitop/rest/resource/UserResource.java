package com.pmease.gitop.rest.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	private final UserManager userManager;
	
	@Inject
	public UserResource(UserManager userManager) {
		this.userManager = userManager;
	}
	
	@GET
	public Collection<User> query(
			@Nullable @QueryParam("name") String name, 
			@Nullable @QueryParam("emailAddress") String emailAddress, 
			@Nullable @QueryParam("fullName") String fullName) {
		List<Criterion> criterions = new ArrayList<>();
		if (name != null)
			criterions.add(Restrictions.eq("name", name));
		if (emailAddress != null)
			criterions.add(Restrictions.eq("emailAddress", emailAddress));
		if (fullName != null)
			criterions.add(Restrictions.eq("fullName", fullName));
		List<User> users = userManager.query(criterions.toArray(new Criterion[criterions.size()]));
		
		for (User user: users) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserRead(user))) {
				throw new UnauthorizedException("Unauthorized access to user " + user.getDisplayName());
			}
		}
		return users;
	}
	
    @GET
    @Path("/{userId}")
    public User get(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserRead(user)))
    		throw new UnauthorizedException();
    	else
    		return user;
    }
    
    @POST
    public Long save(@Valid User user) {
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(user)))
    		throw new UnauthorizedException();
    	
    	userManager.save(user);
    	return user.getId();
    }
    
    @DELETE
    @Path("/{userId}")
    public void delete(@PathParam("userId") Long userId) {
    	User user = userManager.load(userId);
    	
    	if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(user)))
    		throw new UnauthorizedException();
    	
    	userManager.delete(user);
    }
}

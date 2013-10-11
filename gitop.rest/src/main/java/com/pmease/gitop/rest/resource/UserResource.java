package com.pmease.gitop.rest.resource;

import io.dropwizard.jersey.params.LongParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	private final UserManager userManager;
	
	@Inject
	public UserResource(UserManager userManager) {
		this.userManager = userManager;
	}
	
    @GET
    @Path("/{userId}")
    @Timed
    public User get(@PathParam("userId") LongParam userId) {
    	User user = userManager.load(userId.get());
    	user.getName();
    	return user;
    }
    
    @POST
    public Long save(User user) {
    	userManager.save(user);
    	return user.getId();
    }
    
}

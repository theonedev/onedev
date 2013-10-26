package com.pmease.gitop.rest.resource;

import io.dropwizard.jersey.params.LongParam;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
	public List<User> get() {
		return userManager.query();
	}
	
    @GET
    @Path("/{userId}")
    public User get(@PathParam("userId") LongParam userId) {
    	return userManager.load(userId.get());
    }
    
    @POST
    public Long save(@Valid User user) {
    	userManager.save(user);
    	return user.getId();
    }
    
}

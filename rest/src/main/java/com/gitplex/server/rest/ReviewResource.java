package com.gitplex.server.rest;

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
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthorizedException;

import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.User;
import com.gitplex.server.security.SecurityUtils;

@Path("/reviews")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ReviewResource {

	private final ReviewManager reviewManager;
	
	private final UserManager userManager;
	
	@Inject
	public ReviewResource(ReviewManager reviewManager, UserManager userManager) {
		this.reviewManager = reviewManager;
		this.userManager = userManager;
	}
	
    @GET
    @Path("/{id}")
    public Review get(@PathParam("id") Long id) {
    	Review review = reviewManager.load(id);
    	if (!SecurityUtils.canRead(review.getRequest().getTargetProject()))
    		throw new UnauthorizedException();
    	return review;
    }
    
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
    	Review review = reviewManager.load(id);
    	User currentUser = userManager.getCurrent();
    	if (review.getUser().equals(currentUser) || SecurityUtils.canManage(review.getRequest().getTargetProject())) 
    		reviewManager.delete(review);
    	else
    		throw new UnauthorizedException();
    }

    @POST
    public Long save(@NotNull @Valid Review review) {
    	User user = userManager.getCurrent();
    	if (!SecurityUtils.canRead(review.getRequest().getTargetProject()) || user == null)
    		throw new UnauthorizedException();
		
    	if (review.isNew()) {
    		Review existingReview = reviewManager.find(review.getRequest(), user, review.getCommit());
    		if (existingReview != null) {
        		existingReview.setApproved(review.isApproved());
        		existingReview.setAutoCheck(review.isAutoCheck());
        		existingReview.setCheckMerged(review.isCheckMerged());
        		existingReview.setNote(review.getNote());
        		reviewManager.save(existingReview);
        		return existingReview.getId();
    		}
    	} 
    	review.setUser(user);
    	reviewManager.save(review);
    	return review.getId();
    }
    
}

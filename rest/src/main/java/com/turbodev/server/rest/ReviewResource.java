package com.turbodev.server.rest;

import java.util.Collection;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.criterion.Restrictions;

import com.turbodev.server.manager.ReviewManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.Review;
import com.turbodev.server.model.User;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.rest.jersey.ValidQueryParams;
import com.turbodev.server.security.SecurityUtils;

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

    @ValidQueryParams
    @GET
    public Response query(@QueryParam("pullRequest") Long pullRequestId, @QueryParam("user") Long userId, 
    		@QueryParam("commit") String commit, @QueryParam("per_page") Integer perPage, 
    		@QueryParam("page") Integer page, @Context UriInfo uriInfo) {
    	EntityCriteria<Review> criteria = reviewManager.newCriteria();
    	if (pullRequestId != null)
    		criteria.add(Restrictions.eq("request.id", pullRequestId));
    	if (userId != null)
    		criteria.add(Restrictions.eq("user.id", userId));
    	if (commit != null)
    		criteria.add(Restrictions.eq("commit", commit));
    	
    	if (page == null)
    		page = 1;
    	
    	if (perPage == null || perPage > RestConstants.PAGE_SIZE) 
    		perPage = RestConstants.PAGE_SIZE;

    	int totalCount = reviewManager.count(criteria);

    	Collection<Review> reviews = reviewManager.findRange(criteria, (page-1)*perPage, perPage);
		for (Review review: reviews) {
			if (!SecurityUtils.canRead(review.getRequest().getTargetProject())) {
				throw new UnauthorizedException("Unable to access pull request reviews of project '" 
						+ review.getRequest().getTargetProject().getName() + "'");
			}
		}
		
		return Response
				.ok(reviews, RestConstants.JSON_UTF8)
				.links(PageUtils.getNavLinks(uriInfo, totalCount, perPage, page))
				.build();
    }
    
    @Transactional
    @POST
    public Long save(@NotNull @Valid Review review) {
    	if (!SecurityUtils.canRead(review.getRequest().getTargetProject()) 
    			|| !review.getUser().equals(userManager.getCurrent()) && !SecurityUtils.isAdministrator()) {
    		throw new UnauthorizedException();
    	}
    	reviewManager.save(review);
    	return review.getId();
    }
    
}

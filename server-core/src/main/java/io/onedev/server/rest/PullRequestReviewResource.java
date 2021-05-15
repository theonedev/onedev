package io.onedev.server.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.security.SecurityUtils;

@Path("/pull-request-reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestReviewResource {

	private final PullRequestReviewManager reviewManager;

	@Inject
	public PullRequestReviewResource(PullRequestReviewManager reviewManager) {
		this.reviewManager = reviewManager;
	}

	@Path("/{reviewId}")
	@GET
	public PullRequestReview get(@PathParam("reviewId") Long reviewId) {
		PullRequestReview review = reviewManager.load(reviewId);
		if (!SecurityUtils.canReadCode(review.getRequest().getProject()))
			throw new UnauthorizedException();
		return review;
	}
	
	@POST
	public Long save(@NotNull PullRequestReview review) {
		if (!SecurityUtils.canReadCode(review.getRequest().getProject()) 
				|| !SecurityUtils.isAdministrator() && !review.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}
		
		if (review.getRequest().isMerged())
			throw new ExplicitException("Pull request is merged");
		
		if (review.getResult() != null)
			reviewManager.review(review);
		else
			reviewManager.addReviewer(review);
		return review.getId();
	}
	
	@Path("/{reviewId}")
	@DELETE
	public Response delete(@PathParam("reviewId") Long reviewId) {
		PullRequestReview review = reviewManager.load(reviewId);
		if (!SecurityUtils.canReadCode(review.getRequest().getProject()) 
				|| !SecurityUtils.canModify(review.getRequest())) {
			throw new UnauthorizedException();
		}

		if (!reviewManager.removeReviewer(review, Lists.newArrayList())) {
			throw new ExplicitException("Reviewer '" + review.getUser().getDisplayName() 
					+ "' is required and can not be removed");
		}
		
		return Response.ok().build();
	}
	
}

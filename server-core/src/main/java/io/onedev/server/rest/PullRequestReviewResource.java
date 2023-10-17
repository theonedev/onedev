package io.onedev.server.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Api(order=3300)
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

	@Api(order=100)
	@Path("/{reviewId}")
	@GET
	public PullRequestReview get(@PathParam("reviewId") Long reviewId) {
		PullRequestReview review = reviewManager.load(reviewId);
		if (!SecurityUtils.canReadCode(review.getRequest().getProject()))
			throw new UnauthorizedException();
		return review;
	}
	
	@Api(order=200, description="Creater new pull request review")
	@POST
	public Long create(@NotNull PullRequestReview review) {
		if (!SecurityUtils.canReadCode(review.getRequest().getProject()) 
				|| !SecurityUtils.isAdministrator() && !review.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}

		if (review.getUser().equals(review.getRequest().getSubmitter()))
			throw new ExplicitException("Pull request submitter can not be reviewer");
		
		if (review.getRequest().isMerged())
			throw new ExplicitException("Pull request is merged");
		
		reviewManager.create(review);
		return review.getId();
	}

	@Api(order=250, description="Update pull request review of specified id")
	@Path("/{reviewId}")
	@POST
	public Response update(@PathParam("reviewId") Long reviewId, @NotNull PullRequestReview review) {
		if (!SecurityUtils.canReadCode(review.getRequest().getProject())
				|| !SecurityUtils.isAdministrator() && !review.getUser().equals(SecurityUtils.getUser())) {
			throw new UnauthorizedException();
		}

		if (review.getUser().equals(review.getRequest().getSubmitter()))
			throw new ExplicitException("Pull request submitter can not be reviewer");

		if (review.getRequest().isMerged())
			throw new ExplicitException("Pull request is merged");

		reviewManager.update(review);
		return Response.ok().build();
	}
	
}

package io.onedev.server.rest.resource;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
				|| !SecurityUtils.isAdministrator() && !review.getUser().equals(SecurityUtils.getAuthUser())) {
			throw new UnauthorizedException();
		}

		if (review.getUser().equals(review.getRequest().getSubmitter()))
			throw new ExplicitException("Pull request submitter can not be reviewer");
		
		if (review.getRequest().isMerged())
			throw new ExplicitException("Pull request is merged");
		
		reviewManager.createOrUpdate(review);
		return review.getId();
	}

	@Api(order=250, description="Update pull request review of specified id")
	@Path("/{reviewId}")
	@POST
	public Response update(@PathParam("reviewId") Long reviewId, @NotNull PullRequestReview review) {
		if (!SecurityUtils.canModifyOrDelete(review)) 
			throw new UnauthorizedException();

		if (review.getUser().equals(review.getRequest().getSubmitter()))
			throw new ExplicitException("Pull request submitter can not be reviewer");

		if (review.getRequest().isMerged())
			throw new ExplicitException("Pull request is merged");

		reviewManager.createOrUpdate(review);
		return Response.ok().build();
	}
	
}

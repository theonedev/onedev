package io.onedev.server.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;

@Path("/pull-request-reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestReviewResource {

	private final PullRequestService pullRequestService;

	private final PullRequestReviewService pullRequestReviewService;

	@Inject
	public PullRequestReviewResource(PullRequestReviewService pullRequestReviewService, PullRequestService pullRequestService) {
		this.pullRequestService = pullRequestService;
		this.pullRequestReviewService = pullRequestReviewService;
	}

	@Api(order=100)
	@Path("/{reviewId}")
	@GET
	public PullRequestReview get(@PathParam("reviewId") Long reviewId) {
		PullRequestReview review = pullRequestReviewService.load(reviewId);
		if (!SecurityUtils.canReadCode(review.getRequest().getProject()))
			throw new UnauthorizedException();
		return review;
	}
	
	@Api(order=200, description="Create new pull request review")
	@POST
	public Long create(@NotNull PullRequestReview review) {
		var request = review.getRequest();
		if (!SecurityUtils.canModifyPullRequest(request)) {
			throw new UnauthorizedException();
		}

		if (review.getUser().equals(review.getRequest().getSubmitter()))
			throw new ExplicitException("Pull request submitter can not be reviewer");
		
		if (review.getRequest().isMerged())
			throw new ExplicitException("Pull request is merged");
		
		pullRequestReviewService.createOrUpdate(SecurityUtils.getUser(), review);
		return review.getId();
	}

	@Api(order=250, description="Update pull request review of specified id")
	@Path("/{reviewId}")
	@POST
	public Response update(@PathParam("reviewId") Long reviewId, @NotNull PullRequestReview review) {
		if (!SecurityUtils.canModifyOrDelete(review)) 
			throw new UnauthorizedException();

		var request = review.getRequest();
		var reviewer = review.getUser();

		if (request.isMerged())
			throw new ExplicitException("Pull request is merged");
		
		pullRequestService.checkReviews(request, false);
		if (review.getStatus() == PullRequestReview.Status.EXCLUDED) {
			pullRequestReviewService.createOrUpdate(SecurityUtils.getUser(), review);
			return Response.ok().build();
		} else {
			throw new NotAcceptableException("Reviewer '" + reviewer.getDisplayName()
					+ "' is required and can not be removed");
		}
	}
	
}

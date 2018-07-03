package io.onedev.server.web.component.review;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.security.ProjectPrivilege;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.userchoice.AbstractUserChoiceProvider;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

public class ReviewerProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<PullRequest> requestModel;
	
	public ReviewerProvider(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	public void query(String term, int page, Response<UserFacade> response) {
		PullRequest request = requestModel.getObject();
		Collection<UserFacade> users = SecurityUtils.getAuthorizedUsers(request.getTargetProject().getFacade(), 
				ProjectPrivilege.READ);
		for (PullRequestReview review: request.getReviews()) {
			if (review.getExcludeDate() == null && review.getResult() == null)
				users.remove(review.getUser().getFacade());
		}
		users.remove(SecurityUtils.getUser().getFacade());
		
		List<UserFacade> reviewers = new ArrayList<>(users);

		reviewers.sort(Comparator.comparing(UserFacade::getDisplayName));
		
		reviewers = MatchScoreUtils.filterAndSort(reviewers, new MatchScoreProvider<UserFacade>() {

			@Override
			public double getMatchScore(UserFacade object) {
				return object.getMatchScore(term);
			}
			
		});

		int first = page * WebConstants.PAGE_SIZE;
		int last = first + WebConstants.PAGE_SIZE;
		if (last > reviewers.size()) {
			response.addAll(reviewers.subList(first, reviewers.size()));
		} else {
			response.addAll(reviewers.subList(first, last));
			response.setHasMore(last < reviewers.size());
		}
	}

	@Override
	public void detach() {
		requestModel.detach();
		
		super.detach();
	}

}
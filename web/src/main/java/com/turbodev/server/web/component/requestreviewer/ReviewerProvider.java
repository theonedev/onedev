package com.turbodev.server.web.component.requestreviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.turbodev.utils.matchscore.MatchScoreProvider;
import com.turbodev.utils.matchscore.MatchScoreUtils;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.User;
import com.turbodev.server.security.ProjectPrivilege;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.util.QualityCheckStatus;
import com.turbodev.server.util.facade.UserFacade;
import com.turbodev.server.web.WebConstants;
import com.turbodev.server.web.component.select2.Response;
import com.turbodev.server.web.component.userchoice.AbstractUserChoiceProvider;

public class ReviewerProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<PullRequest> requestModel;
	
	public ReviewerProvider(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	public void query(String term, int page, Response<UserFacade> response) {
		PullRequest request = requestModel.getObject();
		QualityCheckStatus checkStatus = request.getQualityCheckStatus();
		Collection<UserFacade> users = SecurityUtils.getAuthorizedUsers(request.getTargetProject().getFacade(), 
				ProjectPrivilege.READ);
		for (User reviewer: checkStatus.getAwaitingReviewers())
			users.remove(reviewer.getFacade());
		for (User reviewer: checkStatus.getEffectiveReviews().keySet())
			users.remove(reviewer.getFacade());
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
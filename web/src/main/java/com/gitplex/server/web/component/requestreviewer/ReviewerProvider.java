package com.gitplex.server.web.component.requestreviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.QualityCheckStatus;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.userchoice.AbstractUserChoiceProvider;

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

		for (Iterator<UserFacade> it = reviewers.iterator(); it.hasNext();) {
			UserFacade user = it.next();
			if (!user.matchesQuery(term))
				it.remove();
		}
		reviewers.sort(Comparator.comparing(UserFacade::getDisplayName));

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
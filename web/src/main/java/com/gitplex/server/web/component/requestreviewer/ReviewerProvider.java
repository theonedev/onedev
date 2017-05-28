package com.gitplex.server.web.component.requestreviewer;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.gitplex.server.model.User;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.userchoice.AbstractUserChoiceProvider;
import com.gitplex.server.web.component.select2.Response;

public class ReviewerProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<PullRequest> requestModel;
	
	public ReviewerProvider(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	public void query(String term, int page, Response<User> response) {
		List<User> reviewers = requestModel.getObject().getRemainingReviewers();

		for (Iterator<User> it = reviewers.iterator(); it.hasNext();) {
			User user = it.next();
			if (!user.matches(term))
				it.remove();
		}
		
		reviewers.sort((user1, user2) -> user1.getDisplayName().compareTo(user2.getDisplayName()));

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
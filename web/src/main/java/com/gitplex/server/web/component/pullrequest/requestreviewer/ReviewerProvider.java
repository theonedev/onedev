package com.gitplex.server.web.component.pullrequest.requestreviewer;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.accountchoice.AbstractAccountChoiceProvider;
import com.gitplex.server.web.component.select2.Response;

public class ReviewerProvider extends AbstractAccountChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<PullRequest> requestModel;
	
	public ReviewerProvider(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	public void query(String term, int page, Response<Account> response) {
		List<Account> reviewers = requestModel.getObject().getPotentialReviewers();

		for (Iterator<Account> it = reviewers.iterator(); it.hasNext();) {
			Account user = it.next();
			if (!user.matches(term))
				it.remove();
		}
		
		reviewers.sort((user1, user2) -> user1.getDisplayName().compareTo(user2.getDisplayName()));

		int first = page * WebConstants.DEFAULT_PAGE_SIZE;
		int last = first + WebConstants.DEFAULT_PAGE_SIZE;
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
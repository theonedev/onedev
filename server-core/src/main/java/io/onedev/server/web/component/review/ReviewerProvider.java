package io.onedev.server.web.component.review;

import static io.onedev.server.util.match.MatchScoreUtils.filterAndSort;

import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.user.choice.AbstractUserChoiceProvider;

public class ReviewerProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<PullRequest> requestModel;
	
	public ReviewerProvider(IModel<PullRequest> requestModel) {
		this.requestModel = requestModel;
	}
	
	@Override
	public void query(String term, int page, Response<User> response) {
		PullRequest request = requestModel.getObject();
		List<User> reviewers = OneDev.getInstance(UserManager.class).queryAndSort(request.getParticipants());

		for (PullRequestReview review: request.getReviews()) {
			if (review.getExcludeDate() == null && review.getResult() == null)
				reviewers.remove(review.getUser());
		}
		
		new ResponseFiller<User>(response).fill(filterAndSort(reviewers, new MatchScoreProvider<User>() {

			@Override
			public double getMatchScore(User object) {
				return object.getMatchScore(term) 
						* (reviewers.size() - reviewers.indexOf(object)) 
						/ reviewers.size();
			}
			
		}), page, WebConstants.PAGE_SIZE);
	}

	@Override
	public void detach() {
		requestModel.detach();
		
		super.detach();
	}

}
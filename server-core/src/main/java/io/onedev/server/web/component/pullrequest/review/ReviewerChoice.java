package io.onedev.server.web.component.pullrequest.review;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.component.user.choice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public class ReviewerChoice extends SelectToAddChoice<User> {

	private final IModel<PullRequest> requestModel;
	
	public ReviewerChoice(String id, IModel<PullRequest> requestModel) {
		super(id, new ReviewerProvider(requestModel));
		
		this.requestModel = requestModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Add reviewer...");
		getSettings().setFormatResult("onedev.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, User user) {
		PullRequest request = requestModel.getObject();
		PullRequestReview review = new PullRequestReview();
		review.setRequest(request);
		review.setUser(user);
		
		if (!request.isNew())
			OneDev.getInstance(PullRequestReviewManager.class).addReviewer(review);
		else
			request.getReviews().add(review);
	};
	
}

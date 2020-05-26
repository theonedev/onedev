package io.onedev.server.web.component.pullrequest.review;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.component.user.choice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public abstract class ReviewerChoice extends SelectToAddChoice<User> {

	public ReviewerChoice(String id) {
		super(id);
		
		setProvider(new ReviewerProvider() {

			@Override
			protected PullRequest getPullRequest() {
				return ReviewerChoice.this.getPullRequest();
			}
			
		});
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

	protected abstract PullRequest getPullRequest();

	@Override
	protected void onSelect(AjaxRequestTarget target, User user) {
		PullRequestReview review = new PullRequestReview();
		review.setRequest(getPullRequest());
		review.setUser(user);
		
		if (!getPullRequest().isNew())
			OneDev.getInstance(PullRequestReviewManager.class).addReviewer(review);
		else
			getPullRequest().getReviews().add(review);
	};
	
}

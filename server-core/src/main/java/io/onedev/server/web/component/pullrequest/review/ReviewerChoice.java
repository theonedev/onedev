package io.onedev.server.web.component.pullrequest.review;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.OneDev;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.web.component.select2.SelectToActChoice;
import io.onedev.server.web.component.user.choice.UserChoiceResourceReference;
import io.onedev.server.web.page.base.BasePage;

public abstract class ReviewerChoice extends SelectToActChoice<User> {

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
		
		getSettings().setPlaceholder(_T("Add reviewer..."));
		getSettings().setTemplateResult("onedev.server.userChoiceFormatter.formatResult");
		getSettings().setTemplateSelection("onedev.server.userChoiceFormatter.formatSelection");
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
		var request = getPullRequest();
		try {
			OneDev.getInstance(PullRequestReviewService.class).addReviewer(SecurityUtils.getUser(), request, user);
			if (!request.isNew())
				((BasePage)getPage()).notifyObservableChange(target, PullRequest.getChangeObservable(request.getId()));
		} catch (NotAcceptableException e) {
			getSession().warn(e.getMessage());
		}
	}

}

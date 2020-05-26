package io.onedev.server.web.component.pullrequest.review;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;

@SuppressWarnings("serial")
public abstract class ReviewerAvatar extends Panel {

	public ReviewerAvatar(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserIdentPanel("avatar", getReview().getUser(), Mode.AVATAR));
		add(new ReviewStatusIcon("status") {

			@Override
			protected ReviewResult getResult() {
				return getReview().getResult();
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ReviewCssResourceReference()));
	}
	
	protected abstract PullRequestReview getReview();
	
}

package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.page.project.pullrequests.detail.activities.SinceChangesLink;

@SuppressWarnings("serial")
class PullRequestChangePanel extends GenericPanel<PullRequestChange> {

	public PullRequestChangePanel(String id, IModel<PullRequestChange> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequestChange change = getModelObject();
		User userForDisplay = User.getForDisplay(change.getUser(), change.getUserName());
		add(new UserLink("user", userForDisplay).setVisible(userForDisplay != null));
		add(new Label("description", change.getData().getDescription()));
		add(new Label("age", DateUtils.formatAge(change.getDate())));
		add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getChange().getRequest();
			}

		}, getChange().getDate()));
		
		add(change.getData().render("body", change));
	}

	private PullRequestChange getChange() {
		return getModelObject();
	}
	
}

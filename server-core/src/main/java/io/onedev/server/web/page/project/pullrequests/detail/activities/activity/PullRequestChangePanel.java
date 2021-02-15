package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.DateUtils;
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

		if (change.getUser() != null) 
			add(new Label("user", change.getUser().getDisplayName()));
		else if (change.getUserName() != null) 
			add(new Label("user", change.getUserName()));
		else
			add(new WebMarkupContainer("user").setVisible(false));
		
		add(new Label("description", change.getData().getActivity(null)));
		add(new Label("age", DateUtils.formatAge(change.getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(change.getDate()))));
		add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getChange().getRequest();
			}

		}, getChange().getDate()));
		
		Component body = change.getData().render("body", change);
		if (body != null) {
			add(body);
		} else {
			add(new WebMarkupContainer("body").setVisible(false));
			add(AttributeAppender.append("class", "no-body"));
		}
	}

	private PullRequestChange getChange() {
		return getModelObject();
	}
	
}

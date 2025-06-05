package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.IssueChange;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;

class IssueChangePanel extends Panel {

	public IssueChangePanel(String id) {
		super(id);
	}
		
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueChange change = getChange();

		add(new UserIdentPanel("user", change.getUser(), Mode.AVATAR_AND_NAME));
		add(new Label("description", change.getData().getActivity()));
		
		add(new Label("age", DateUtils.formatAge(change.getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(change.getDate()))));
		
		ActivityDetail detail = getChange().getData().getActivityDetail();
		if (detail != null) 
			add(detail.render("body"));
		else 
			add(new WebMarkupContainer("body").setVisible(false));		

		setMarkupId(getChange().getAnchor());
		setOutputMarkupId(true);	
	}

	private IssueChange getChange() {
		return ((IssueChangeActivity) getDefaultModelObject()).getChange();
	}

}

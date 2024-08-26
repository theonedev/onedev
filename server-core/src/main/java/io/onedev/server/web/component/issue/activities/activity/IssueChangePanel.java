package io.onedev.server.web.component.issue.activities.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.IssueChange;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.DateUtils;

@SuppressWarnings("serial")
class IssueChangePanel extends GenericPanel<IssueChange> {

	public IssueChangePanel(String id, IModel<IssueChange> model) {
		super(id, model);
	}
	
	private IssueChange getChange() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String activity = getChange().getData().getActivity();
		if (getChange().getUser() != null) {
			add(new Label("user", getChange().getUser().getDisplayName()));
		} else {
			add(new WebMarkupContainer("user").setVisible(false));
			activity = StringUtils.capitalize(activity);
		}

		add(new Label("description", activity));
		
		add(new Label("age", DateUtils.formatAge(getChange().getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(getChange().getDate()))));
		
		ActivityDetail detail = getChange().getData().getActivityDetail();
		if (detail != null) {
			add(detail.render("detail"));
		} else {
			add(new WebMarkupContainer("detail").setVisible(false));
			add(AttributeAppender.append("class", "no-body"));
		}
	}

}

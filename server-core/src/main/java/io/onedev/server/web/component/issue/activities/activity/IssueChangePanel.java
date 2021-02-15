package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.IssueChange;
import io.onedev.server.util.DateUtils;

@SuppressWarnings("serial")
class IssueChangePanel extends GenericPanel<IssueChange> {

	public IssueChangePanel(String id, IModel<IssueChange> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueChange change = getModelObject();
		if (change.getUser() != null) 
			add(new Label("user", change.getUser().getDisplayName()));
		else if (change.getUserName() != null) 
			add(new Label("user", change.getUserName()));
		else
			add(new WebMarkupContainer("user").setVisible(false));

		add(new Label("description", change.getData().getActivity(null)));
		add(new Label("age", DateUtils.formatAge(change.getDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(change.getDate()))));
		
		add(change.getData().render("body", change));
	}

}

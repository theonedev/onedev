package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.link.UserLink;

@SuppressWarnings("serial")
class IssueChangePanel extends GenericPanel<IssueChange> {

	public IssueChangePanel(String id, IModel<IssueChange> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueChange change = getModelObject();
		User userForDisplay = User.getForDisplay(change.getUser(), change.getUserName());
		add(new UserLink("user", userForDisplay).setVisible(userForDisplay != null));
		add(new Label("description", change.getData().getDescription()));
		add(new Label("age", DateUtils.formatAge(change.getDate())));
		
		add(change.getData().render("body", change));
	}

}

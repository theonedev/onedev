package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.IssueChange;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.SystemUserIdent;
import io.onedev.server.util.userident.UserIdent;

@SuppressWarnings("serial")
class IssueChangePanel extends GenericPanel<IssueChange> {

	public IssueChangePanel(String id, IModel<IssueChange> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueChange change = getModelObject();
		UserIdent userIdent = UserIdent.of(UserFacade.of(change.getUser()), change.getUserName());
		add(new Label("user", userIdent.getName()).setVisible(!(userIdent instanceof SystemUserIdent)));
		add(new Label("description", change.getData().getDescription()));
		add(new Label("age", DateUtils.formatAge(change.getDate())));
		
		add(change.getData().render("body", change));
	}

}

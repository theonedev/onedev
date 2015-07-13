package com.pmease.gitplex.web.page.repository.pullrequest.requestactivity;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.userlink.UserLink;

@SuppressWarnings("serial")
public class DisapproveActivityPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<User> userModel;
	
	private final Date date;
	
	public DisapproveActivityPanel(String id, IModel<PullRequest> requestModel, IModel<User> userModel, Date date) {
		super(id);
		this.requestModel = requestModel;
		this.userModel = userModel;
		this.date = date;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", Model.of(date)));
		add(new SinceChangesLink("changes", requestModel, date, "Changes since this disapproval"));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		userModel.detach();
		super.onDetach();
	}
}

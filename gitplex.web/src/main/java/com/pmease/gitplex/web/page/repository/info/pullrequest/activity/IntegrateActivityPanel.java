package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.UserLink;

@SuppressWarnings("serial")
public class IntegrateActivityPanel extends Panel {

	private final IModel<User> userModel;
	
	private final Date date;
	
	private final String reason;
	
	public IntegrateActivityPanel(String id, IModel<User> userModel, Date date, String reason) {
		super(id);
		this.userModel = userModel;
		this.date = date;
		this.reason = reason;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", Model.of(date)));

		add(new Label("reason", reason).setVisible(reason != null));
	}

	@Override
	protected void onDetach() {
		userModel.detach();
		super.onDetach();
	}
}

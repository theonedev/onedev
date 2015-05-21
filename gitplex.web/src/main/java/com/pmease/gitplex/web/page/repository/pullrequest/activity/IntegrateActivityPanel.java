package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.userlink.UserLink;

@SuppressWarnings("serial")
public class IntegrateActivityPanel extends Panel {

	private final IModel<User> userModel;
	
	private final Date date;
	
	public IntegrateActivityPanel(String id, IModel<User> userModel, Date date) {
		super(id);
		this.userModel = userModel;
		this.date = date;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", Model.of(date)));
	}

	@Override
	protected void onDetach() {
		userModel.detach();
		super.onDetach();
	}
}

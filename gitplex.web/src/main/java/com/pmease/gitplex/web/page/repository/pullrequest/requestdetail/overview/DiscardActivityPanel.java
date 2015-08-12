package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class DiscardActivityPanel extends Panel {

	private final IModel<User> userModel;
	
	private final Date date;
	
	public DiscardActivityPanel(String id, IModel<User> userModel, Date date) {
		super(id);
		this.userModel = userModel;
		this.date = date;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", DateUtils.formatAge(date)));
	}

	@Override
	protected void onDetach() {
		userModel.detach();
		super.onDetach();
	}

}

package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.utils.DateUtils;

@SuppressWarnings("serial")
class ApproveActivityPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<User> userModel;
	
	private final Date date;
	
	public ApproveActivityPanel(String id, IModel<PullRequest> requestModel, IModel<User> userModel, Date date) {
		super(id);
		this.requestModel = requestModel;
		this.userModel = userModel;
		this.date = date;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", userModel));
		add(new Label("age", DateUtils.formatAge(date)));
		add(new SinceChangesLink("changes", requestModel, date, "Changes since this approval"));
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		userModel.detach();
		super.onDetach();
	}

}

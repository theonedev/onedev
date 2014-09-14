package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.model.UserModel;

public class DisapprovePullRequest implements PullRequestActivity {

	private final User user;
	
	private final Date date;
	
	public DisapprovePullRequest(User user, Date date) {
		this.user = user;
		this.date = date;
	}
	
	@Override
	public Panel render(String panelId) {
		return new DisapproveActivityPanel(panelId, new UserModel(user), date);
	}

	public User getUser() {
		return user;
	}

	public Date getDate() {
		return date;
	}

}

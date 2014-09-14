package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.model.UserModel;

public class IntegratePullRequest implements PullRequestActivity {

	private final User user;
	
	private final Date date;
	
	private final String reason;
	
	public IntegratePullRequest(User user, Date date, String reason) {
		this.user = user;
		this.date = date;
		this.reason = reason;
	}
	
	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public User getUser() {
		return user;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public Panel render(String panelId) {
		return new IntegrateActivityPanel(panelId, new UserModel(user), date, reason);
	}

}

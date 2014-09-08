package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.model.UserModel;

public class DiscardPullRequest implements PullRequestActivity {

	private final User user;
	
	private final Date date;
	
	public DiscardPullRequest(User user, Date date) {
		this.user = user;
		this.date = date;
	}
	
	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public boolean isDiscussion() {
		return false;
	}

	@Override
	public Panel render(String panelId) {
		return new DiscardActivityPanel(panelId, new UserModel(user), date);
	}

}

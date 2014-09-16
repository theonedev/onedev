package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class ApprovePullRequest implements PullRequestActivity {

	private final Long userId;
	
	private final Date date;
	
	public ApprovePullRequest(User user, Date date) {
		this.userId = user.getId();
		this.date = date;
	}
	
	@Override
	public Panel render(String panelId) {
		return new ApproveActivityPanel(panelId, new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return getUser();
			}
			
		}, date);
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public User getUser() {
		return GitPlex.getInstance(Dao.class).load(User.class, userId);
	}

}

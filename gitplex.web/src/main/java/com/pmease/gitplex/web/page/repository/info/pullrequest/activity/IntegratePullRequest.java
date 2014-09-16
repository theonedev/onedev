package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class IntegratePullRequest implements PullRequestActivity {

	private final Long userId;
	
	private final Date date;
	
	private final String reason;
	
	public IntegratePullRequest(User user, Date date, String reason) {
		this.userId = user.getId();
		this.date = date;
		this.reason = reason;
	}
	
	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public User getUser() {
		return GitPlex.getInstance(Dao.class).load(User.class, userId);
	}

	public String getReason() {
		return reason;
	}

	@Override
	public Panel render(String panelId) {
		return new IntegrateActivityPanel(panelId, new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return getUser();
			}
			
		}, date, reason);
	}

}

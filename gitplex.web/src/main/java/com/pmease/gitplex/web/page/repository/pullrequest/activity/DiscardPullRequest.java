package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class DiscardPullRequest implements PullRequestActivity {

	private final Long userId;
	
	private final Date date;
	
	public DiscardPullRequest(@Nullable User user, Date date) {
		this.userId = user!=null?user.getId():null;
		this.date = date;
	}
	
	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public User getUser() {
		if (userId != null)
			return GitPlex.getInstance(Dao.class).load(User.class, userId);
		else
			return null;
	}

	@Override
	public Panel render(String panelId) {
		return new DiscardActivityPanel(panelId, new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return getUser();
			}
			
		}, date);
	}

}

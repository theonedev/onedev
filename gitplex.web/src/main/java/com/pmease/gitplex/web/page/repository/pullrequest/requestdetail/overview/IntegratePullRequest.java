package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
class IntegratePullRequest implements RenderableActivity {

	private final Long userId;
	
	private final Date date;
	
	public IntegratePullRequest(User user, Date date) {
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
		return new IntegrateActivityPanel(panelId, new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return getUser();
			}
			
		}, date);
	}

}

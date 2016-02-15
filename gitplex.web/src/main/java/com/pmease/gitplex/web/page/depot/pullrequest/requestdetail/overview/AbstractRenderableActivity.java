package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.util.Date;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestActivity;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
abstract class AbstractRenderableActivity implements RenderableActivity {

	private final Long requestId;
	
	private final Long userId;
	
	private final Date date;
	
	public AbstractRenderableActivity(PullRequest request, User user, Date date) {
		this.requestId = request.getId();
		this.userId = user!=null?user.getId():null;
		this.date = date;
	}
	
	public AbstractRenderableActivity(PullRequestActivity activity) {
		this(activity.getRequest(), activity.getUser(), activity.getDate());
	}
	
	@Override
	public PullRequest getRequest() {
		return GitPlex.getInstance(Dao.class).load(PullRequest.class, requestId);
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

}

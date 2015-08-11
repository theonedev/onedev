package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
class OpenPullRequest implements RenderableActivity {

	private final Long requestId;
	
	public OpenPullRequest(PullRequest request) {
		this.requestId = request.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new OpenActivityPanel(panelId, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return getRequest();
			}
			
		});
	}

	public PullRequest getRequest() {
		return GitPlex.getInstance(Dao.class).load(PullRequest.class, requestId);
	}
	
	@Override
	public Date getDate() {
		return getRequest().getCreateDate();
	}

	@Override
	public User getUser() {
		return getRequest().getSubmitter();
	}

}

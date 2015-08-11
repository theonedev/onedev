package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class UpdatePullRequest implements RenderableActivity {

	private final Long updateId;
	
	public UpdatePullRequest(PullRequestUpdate update) {
		this.updateId = update.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new UpdateActivityPanel(panelId, new LoadableDetachableModel<PullRequestUpdate>() {

			@Override
			protected PullRequestUpdate load() {
				return getUpdate();
			}
			
		});
	}

	@Override
	public Date getDate() {
		return getUpdate().getDate();
	}

	@Override
	public User getUser() {
		return null;
	}

	public PullRequestUpdate getUpdate() {
		return GitPlex.getInstance(Dao.class).load(PullRequestUpdate.class, updateId);
	}

}

package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

@SuppressWarnings("serial")
public class UpdatePullRequest extends AbstractRenderableActivity {

	private final Long updateId;
	
	public UpdatePullRequest(PullRequestUpdate update) {
		super(update.getRequest(), null, update.getDate());
		this.updateId = update.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new UpdateActivityPanel(panelId, this, new LoadableDetachableModel<PullRequestUpdate>() {

			@Override
			protected PullRequestUpdate load() {
				return getUpdate();
			}
			
		});
	}

	public PullRequestUpdate getUpdate() {
		return GitPlex.getInstance(Dao.class).load(PullRequestUpdate.class, updateId);
	}

}

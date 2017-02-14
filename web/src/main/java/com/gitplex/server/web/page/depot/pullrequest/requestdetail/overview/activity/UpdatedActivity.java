package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.PullRequestUpdate;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class UpdatedActivity implements PullRequestActivity {

	private final Long updateId;
	
	public UpdatedActivity(PullRequestUpdate update) {
		updateId = update.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new UpdatedPanel(panelId, new LoadableDetachableModel<PullRequestUpdate>() {

			@Override
			protected PullRequestUpdate load() {
				return getUpdate();
			}
			
		});
	}

	public PullRequestUpdate getUpdate() {
		return GitPlex.getInstance(Dao.class).load(PullRequestUpdate.class, updateId);
	}

	@Override
	public Date getDate() {
		return getUpdate().getDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

}

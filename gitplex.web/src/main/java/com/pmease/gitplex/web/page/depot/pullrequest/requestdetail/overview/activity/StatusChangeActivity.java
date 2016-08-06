package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequestStatusChange;
import com.pmease.gitplex.core.manager.PullRequestStatusChangeManager;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class StatusChangeActivity implements PullRequestActivity {

	private final Long statusChangeId;
	
	public StatusChangeActivity(PullRequestStatusChange statusChange) {
		statusChangeId = statusChange.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new StatusChangePanel(panelId, new LoadableDetachableModel<PullRequestStatusChange>() {

			@Override
			protected PullRequestStatusChange load() {
				return getStatusChange();
			}
			
		});
	}

	private PullRequestStatusChange getStatusChange() {
		return GitPlex.getInstance(PullRequestStatusChangeManager.class).load(statusChangeId);
	}

	@Override
	public Date getDate() {
		return getStatusChange().getDate();
	}
	
}

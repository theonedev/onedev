package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.PullRequestStatusChange;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.PullRequestActivity;

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

	public PullRequestStatusChange getStatusChange() {
		return GitPlex.getInstance(PullRequestStatusChangeManager.class).load(statusChangeId);
	}

	@Override
	public Date getDate() {
		return getStatusChange().getDate();
	}

	@Override
	public String getAnchor() {
		return getStatusChange().getAnchor();
	}

}

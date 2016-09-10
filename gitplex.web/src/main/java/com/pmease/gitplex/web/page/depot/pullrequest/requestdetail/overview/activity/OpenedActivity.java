package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class OpenedActivity implements PullRequestActivity {

	private final Long requestId;
	
	public OpenedActivity(PullRequest request) {
		requestId = request.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new OpenedPanel(panelId, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return getRequest();
			}
			
		});
	}

	public PullRequest getRequest() {
		return GitPlex.getInstance(PullRequestManager.class).load(requestId);
	}
	
	@Override
	public Date getDate() {
		return getRequest().getSubmitDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

}

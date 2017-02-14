package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.PullRequestReference;
import com.gitplex.server.core.manager.PullRequestReferenceManager;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.PullRequestActivity;

@SuppressWarnings("serial")
public class ReferencedActivity implements PullRequestActivity {

	private final Long referenceId;
	
	public ReferencedActivity(PullRequestReference reference) {
		referenceId = reference.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new ReferencedPanel(panelId, new LoadableDetachableModel<PullRequestReference>() {

			@Override
			protected PullRequestReference load() {
				return getReference();
			}
			
		});
	}

	private PullRequestReference getReference() {
		return GitPlex.getInstance(PullRequestReferenceManager.class).load(referenceId);
	}
	@Override
	public Date getDate() {
		return getReference().getDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

}

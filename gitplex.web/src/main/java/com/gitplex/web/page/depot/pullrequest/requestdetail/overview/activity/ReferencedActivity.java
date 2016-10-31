package com.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.PullRequestReference;
import com.gitplex.core.manager.PullRequestReferenceManager;
import com.gitplex.web.page.depot.pullrequest.requestdetail.overview.PullRequestActivity;

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

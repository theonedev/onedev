package com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.PullRequestReferenceManager;
import com.turbodev.server.model.PullRequestReference;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.PullRequestActivity;

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
		return TurboDev.getInstance(PullRequestReferenceManager.class).load(referenceId);
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

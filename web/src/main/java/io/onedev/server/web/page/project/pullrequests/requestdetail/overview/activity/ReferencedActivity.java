package io.onedev.server.web.page.project.pullrequests.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestReferenceManager;
import io.onedev.server.model.PullRequestReference;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.PullRequestActivity;

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
		return OneDev.getInstance(PullRequestReferenceManager.class).load(referenceId);
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

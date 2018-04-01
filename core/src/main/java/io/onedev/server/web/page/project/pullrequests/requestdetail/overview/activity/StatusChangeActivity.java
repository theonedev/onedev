package io.onedev.server.web.page.project.pullrequests.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestStatusChangeManager;
import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.PullRequestActivity;

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
		return OneDev.getInstance(PullRequestStatusChangeManager.class).load(statusChangeId);
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

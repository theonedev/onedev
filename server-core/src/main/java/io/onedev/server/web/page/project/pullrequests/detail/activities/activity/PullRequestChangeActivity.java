package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;

public class PullRequestChangeActivity implements PullRequestActivity {

	private final Long changeId;
	
	public PullRequestChangeActivity(PullRequestChange change) {
		changeId = change.getId();
	}
	
	@Override
	public Component render(String panelId) {
		return new PullRequestChangePanel(panelId);
	}

	public PullRequestChange getChange() {
		return OneDev.getInstance(PullRequestChangeService.class).load(changeId);
	}

	@Override
	public Date getDate() {
		return getChange().getDate();
	}

}

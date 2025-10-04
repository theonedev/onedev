package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestUpdateService;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;

public class PullRequestUpdateActivity implements PullRequestActivity {

	private final Long updateId;
	
	public PullRequestUpdateActivity(PullRequestUpdate update) {
		updateId = update.getId();
	}
	
	@Override
	public Component render(String componentId) {
		return new PullRequestUpdatePanel(componentId);
	}

	public PullRequestUpdate getUpdate() {
		return OneDev.getInstance(PullRequestUpdateService.class).load(updateId);
	}

	@Override
	public Date getDate() {
		return getUpdate().getDate();
	}

}

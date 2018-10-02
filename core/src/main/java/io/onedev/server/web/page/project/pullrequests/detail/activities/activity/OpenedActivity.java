package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class OpenedActivity implements PullRequestActivity {

	private final Long requestId;
	
	public OpenedActivity(PullRequest request) {
		requestId = request.getId();
	}
	
	@Override
	public Component render(String componentId, DeleteCallback deleteCallback) {
		return new OpenedPanel(componentId, new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return getRequest();
			}
			
		});
	}

	public PullRequest getRequest() {
		return OneDev.getInstance(PullRequestManager.class).load(requestId);
	}
	
	@Override
	public Date getDate() {
		return getRequest().getSubmitDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

	@Override
	public User getUser() {
		return User.getForDisplay(getRequest().getSubmitter(), getRequest().getSubmitterName());
	}

}

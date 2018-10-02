package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestActionManager;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.User;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class ActionActivity implements PullRequestActivity {

	private final Long actionId;
	
	public ActionActivity(PullRequestAction action) {
		actionId = action.getId();
	}
	
	@Override
	public Component render(String panelId, DeleteCallback callback) {
		return getAction().getData().render(panelId, getAction());
	}

	private PullRequestAction getAction() {
		return OneDev.getInstance(PullRequestActionManager.class).load(actionId);
	}

	@Override
	public Date getDate() {
		return getAction().getDate();
	}

	@Override
	public String getAnchor() {
		return getAction().getAnchor();
	}

	@Override
	public User getUser() {
		return User.getForDisplay(getAction().getUser(), getAction().getUserName());
	}

}

package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.User;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class PullRequestChangeActivity implements PullRequestActivity {

	private final Long changeId;
	
	public PullRequestChangeActivity(PullRequestChange change) {
		changeId = change.getId();
	}
	
	@Override
	public Component render(String panelId, DeleteCallback callback) {
		return new PullRequestChangePanel(panelId, new LoadableDetachableModel<PullRequestChange>() {

			@Override
			protected PullRequestChange load() {
				return getChange();
			}
			
		});
	}

	private PullRequestChange getChange() {
		return OneDev.getInstance(PullRequestChangeManager.class).load(changeId);
	}

	@Override
	public Date getDate() {
		return getChange().getDate();
	}

	@Override
	public String getAnchor() {
		return getChange().getAnchor();
	}

	@Override
	public User getUser() {
		if (getChange().getUser() != null || getChange().getUserName() != null)
			return User.from(getChange().getUser(), getChange().getUserName());
		else
			return null;
	}

}

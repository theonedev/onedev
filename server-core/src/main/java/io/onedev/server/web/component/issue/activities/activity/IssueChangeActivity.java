package io.onedev.server.web.component.issue.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class IssueChangeActivity implements IssueActivity {

	private final Long changeId;
	
	public IssueChangeActivity(IssueChange change) {
		changeId = change.getId();
	}
	
	@Override
	public Panel render(String panelId, DeleteCallback callback) {
		return new IssueChangePanel(panelId, new LoadableDetachableModel<IssueChange>() {

			@Override
			protected IssueChange load() {
				return getChange();
			}
			
		});
	}

	public IssueChange getChange() {
		return OneDev.getInstance(IssueChangeManager.class).load(changeId);
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

package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueActionManager;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.User;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class ActionActivity implements IssueActivity {

	private final Long actionId;
	
	public ActionActivity(IssueAction action) {
		actionId = action.getId();
	}
	
	@Override
	public Panel render(String panelId, DeleteCallback callback) {
		return new ActionPanel(panelId, new LoadableDetachableModel<IssueAction>() {

			@Override
			protected IssueAction load() {
				return getChange();
			}
			
		});
	}

	public IssueAction getChange() {
		return OneDev.getInstance(IssueActionManager.class).load(actionId);
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
		return User.getForDisplay(getChange().getUser(), getChange().getUserName());
	}

}

package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.model.IssueChange;

@SuppressWarnings("serial")
public class ChangedActivity implements IssueActivity {

	private final Long changeId;
	
	public ChangedActivity(IssueChange change) {
		changeId = change.getId();
	}
	
	@Override
	public Panel render(String panelId, ActivityCallback callback) {
		return new ChangedPanel(panelId, new LoadableDetachableModel<IssueChange>() {

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
		return null;
	}

}

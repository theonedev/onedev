package io.onedev.server.web.component.issue.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.model.IssueChange;

public class IssueChangeActivity implements IssueActivity {

	private final Long changeId;
	
	public IssueChangeActivity(IssueChange change) {
		changeId = change.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new IssueChangePanel(panelId);
	}

	public IssueChange getChange() {
		return OneDev.getInstance(IssueChangeService.class).load(changeId);
	}
	
	@Override
	public Date getDate() {
		return getChange().getDate();
	}

}

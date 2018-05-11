package io.onedev.server.web.page.project.issues.issuedetail.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;

@SuppressWarnings("serial")
public class OpenedActivity implements IssueActivity {

	private final Long issueId;
	
	public OpenedActivity(Issue issue) {
		issueId = issue.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new OpenedPanel(panelId, new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				return getIssue();
			}
			
		});
	}

	public Issue getIssue() {
		return OneDev.getInstance(IssueManager.class).load(issueId);
	}
	
	@Override
	public Date getDate() {
		return getIssue().getSubmitDate();
	}

	@Override
	public String getAnchor() {
		return null;
	}

}

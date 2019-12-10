package io.onedev.server.web.component.issue.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.web.util.DeleteCallback;

@SuppressWarnings("serial")
public class IssueOpenedActivity implements IssueActivity {

	private final Long issueId;
	
	public IssueOpenedActivity(Issue issue) {
		issueId = issue.getId();
	}
	
	@Override
	public Panel render(String panelId, DeleteCallback deleteCallback) {
		return new IssueOpenedPanel(panelId, new LoadableDetachableModel<Issue>() {

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

	@Override
	public User getUser() {
		return User.from(getIssue().getSubmitter(), getIssue().getSubmitterName());
	}
	
}

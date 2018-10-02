package io.onedev.server.web.page.project.issues.issuedetail;

import java.util.List;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.commit.list.CommitListPanel;

@SuppressWarnings("serial")
public class IssueCommitsPage extends IssueDetailPage {

	public IssueCommitsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new CommitListPanel("commits", new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}, new AbstractReadOnlyModel<List<RevCommit>>() {

			@Override
			public List<RevCommit> getObject() {
				return getIssue().getCommits();
			}
			
		}));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}

}

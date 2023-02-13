package io.onedev.server.web.component.issue.commits;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.web.component.commit.info.CommitInfoPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import java.util.List;

@SuppressWarnings("serial")
public class IssueCommitsPanel extends GenericPanel<Issue> {

	public IssueCommitsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<ProjectScopedCommit>("commits", new AbstractReadOnlyModel<List<ProjectScopedCommit>>() {

			@Override
			public List<ProjectScopedCommit> getObject() {
				return getIssue().getCommits();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<ProjectScopedCommit> item) {
				item.add(new CommitInfoPanel("commit", item.getModel()) {

					@Override
					protected Project getProject() {
						return getIssue().getProject();
					}
					
				});
			}
			
		});
		
	}

	private Issue getIssue() {
		return getModelObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueCommitsCssResourceReference()));
	}

}

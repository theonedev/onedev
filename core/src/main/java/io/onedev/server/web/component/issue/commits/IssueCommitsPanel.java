package io.onedev.server.web.component.issue.commits;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

@SuppressWarnings("serial")
public class IssueCommitsPanel extends GenericPanel<Issue> {

	public IssueCommitsPanel(String id, IModel<Issue> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<RevCommit>("commits", new AbstractReadOnlyModel<List<RevCommit>>() {

			@Override
			public List<RevCommit> getObject() {
				return getIssue().getCommits();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<RevCommit> item) {
				RevCommit commit = item.getModelObject();
				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(getIssue().getProject(), commitState);

				Link<Void> messageLink = new ViewStateAwarePageLink<Void>("message", CommitDetailPage.class, params);
				messageLink.add(new Label("label", commit.getShortMessage()));
				item.add(messageLink);
				
				Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hash", CommitDetailPage.class, params);
				hashLink.add(new Label("label", GitUtils.abbreviateSHA(commit.name())));
				item.add(hashLink);
				item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));
			}
			
		});
		
	}

	private Issue getIssue() {
		return getModelObject();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canReadCode(getIssue().getProject().getFacade()) 
				&& !getIssue().getCommits().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueCommitsCssResourceReference()));
	}

}

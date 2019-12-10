package io.onedev.server.web.component.issue.activities.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.commit.status.CommitStatusPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.PersonIdentPanel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

@SuppressWarnings("serial")
public abstract class IssueCommittedPanel extends GenericPanel<List<RevCommit>> {

	private final List<ObjectId> commitIds;
	
	public IssueCommittedPanel(String id, List<ObjectId> commitIds) {
		super(id);
		this.commitIds = commitIds;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ListView<RevCommit>("commits", new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				List<RevCommit> commits = new ArrayList<>();
				for (ObjectId commitId: commitIds) {
					RevCommit commit = getIssue().getProject().getRevCommit(commitId, false); 
					if (commit != null)
						commits.add(commit);
				}
				return commits;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<RevCommit> item) {
				RevCommit commit = item.getModelObject();
				
				item.add(new PersonIdentPanel("author", commit.getAuthorIdent(), "Author", Mode.AVATAR));

				item.add(new CommitMessagePanel("message", item.getModel()) {

					@Override
					protected Project getProject() {
						return getIssue().getProject(); 
					}
					
				});

				item.add(new CommitStatusPanel("buildStatus", commit.copy()) {
					
					@Override
					protected String getCssClasses() {
						return "btn btn-default btn-xs";
					}

					@Override
					protected Project getProject() {
						return getIssue().getProject();
					}
					
				});
				
				Project project = getIssue().getProject();
				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(project, commitState);
				Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

				BlobIdent blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
				ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
				params = ProjectBlobPage.paramsOf(project, browseState);
				item.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
			}
			
		});
	}

	protected abstract Issue getIssue();
	
}

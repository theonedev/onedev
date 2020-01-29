package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
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
public abstract class IssueCommittedPanel extends GenericPanel<RevCommit> {

	private final String commitHash;
	
	public IssueCommittedPanel(String id, String commitHash) {
		super(id);
		this.commitHash = commitHash;
		
		setModel(new LoadableDetachableModel<RevCommit>() {

			@Override
			protected RevCommit load() {
				return getIssue().getProject().getRevCommit(commitHash, true);
			}
			
		});
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		RevCommit commit = getModelObject();
		
		WebMarkupContainer container = new WebMarkupContainer("commit");
		add(container);
		
		container.add(new PersonIdentPanel("author", commit.getAuthorIdent(), "Author", Mode.AVATAR));

		container.add(new CommitMessagePanel("message", getModel()) {

			@Override
			protected Project getProject() {
				return getIssue().getProject(); 
			}
			
		});

		CommitStatusPanel commitStatus = new CommitStatusPanel("buildStatus", ObjectId.fromString(commitHash)) {
			
			@Override
			protected String getCssClasses() {
				return "btn btn-default btn-xs";
			}

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}
			
		};
		container.add(commitStatus);
		
		Project project = getIssue().getProject();
		CommitDetailPage.State commitState = new CommitDetailPage.State();
		commitState.revision = commit.name();
		PageParameters params = CommitDetailPage.paramsOf(project, commitState);
		Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
		container.add(hashLink);
		hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
		container.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

		BlobIdent blobIdent = new BlobIdent(commit.name(), null, FileMode.TYPE_TREE);
		ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
		params = ProjectBlobPage.paramsOf(project, browseState);
		container.add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
		
		container.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				commitStatus.configure();
				if (commitStatus.isVisible())
					return "commit with-status";
				else
					return "commit";
			}
			
		}));
		
	}	

	protected abstract Issue getIssue();
	
}

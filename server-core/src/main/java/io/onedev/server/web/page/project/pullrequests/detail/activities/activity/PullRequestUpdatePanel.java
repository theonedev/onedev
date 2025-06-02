package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import com.google.common.collect.Sets;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.gitsignature.SignatureStatusPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.PersonIdentPanel;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

class PullRequestUpdatePanel extends Panel {

	public PullRequestUpdatePanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String tooManyMessage = MessageFormat.format(_T("Too many commits, displaying recent {0}"), WebConstants.MAX_DISPLAY_COMMITS);
		add(new Label("tooManyCommits", tooManyMessage) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUpdate().getCommits().size()>WebConstants.MAX_DISPLAY_COMMITS);
			}
			
		});
		add(new ListView<RevCommit>("commits", new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				List<RevCommit> commits = getUpdate().getCommits();
				if (commits.size() > WebConstants.MAX_DISPLAY_COMMITS)
					return commits.subList(commits.size()-WebConstants.MAX_DISPLAY_COMMITS, commits.size());
				else 
					return commits;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<RevCommit> item) {
				RevCommit commit = item.getModelObject();
				if (!getUpdate().getRequest().getPendingCommits().contains(commit)) {
					item.add(AttributeAppender.append("class", "rebased"));
					item.add(AttributeAppender.append("data-tippy-content", _T("This commit is rebased")));
				}
				
				item.add(new PersonIdentPanel("author", commit.getAuthorIdent(), "Author", Mode.AVATAR));

				item.add(new CommitMessagePanel("message", item.getModel()) {

					@Override
					protected Project getProject() {
						return getUpdate().getRequest().getTarget().getProject(); 
					}
					
				});

				var committer = commit.getCommitterIdent();
				item.add(new Label("date", DateUtils.formatAge(committer.getWhen()))
						.add(new AttributeAppender("title", DateUtils.formatDateTime(committer.getWhen()))));

				item.add(new SignatureStatusPanel("signature") {
					
					@Override
					protected String getIconClass() {
						return "icon icon-sm";
					}

					@Override
					protected RevObject getRevObject() {
						return item.getModelObject();
					}
					
				});
				
				Project project = getUpdate().getRequest().getTarget().getProject();
				CommitDetailPage.State commitState = new CommitDetailPage.State();
				commitState.revision = commit.name();
				PageParameters params = CommitDetailPage.paramsOf(project, commitState);
				Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				item.add(hashLink);
				hashLink.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(new CopyToClipboardLink("copyHash", Model.of(commit.name())));
			}
			
		});
		
		add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(PullRequest.getChangeObservable(getUpdate().getRequest().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	private PullRequestUpdate getUpdate() {
		return ((PullRequestUpdateActivity) getDefaultModelObject()).getUpdate();
	}
	
}

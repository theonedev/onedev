package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Verification;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.component.pullrequest.verificationstatus.VerificationStatusPanel;
import com.pmease.gitplex.web.page.repository.file.HistoryState;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
class UpdateActivityPanel extends Panel {

	private final IModel<PullRequestUpdate> updateModel;
	
	private final IModel<Set<String>> mergedCommitsModel = new LoadableDetachableModel<Set<String>>() {

		@Override
		protected Set<String> load() {
			Set<String> hashes = new HashSet<>();

			for (Commit commit: updateModel.getObject().getRequest().getMergedCommits())
				hashes.add(commit.getHash());
			return hashes;
		}
		
	};

	public UpdateActivityPanel(String id, IModel<PullRequestUpdate> model) {
		super(id);
		
		this.updateModel = model;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String tooManyMessage = "Too many commits, displaying recent " + Constants.MAX_DISPLAY_COMMITS;
		add(new Label("tooManyCommits", tooManyMessage) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(updateModel.getObject().getCommits().size()>Constants.MAX_DISPLAY_COMMITS);
			}
			
		});
		add(new ListView<Commit>("commits", new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				List<Commit> commits = updateModel.getObject().getCommits();
				if (commits.size() > Constants.MAX_DISPLAY_COMMITS)
					return commits.subList(commits.size()-Constants.MAX_DISPLAY_COMMITS, commits.size());
				else 
					return commits;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				
				item.add(new PersonLink("author", Model.of(commit.getAuthor()), AvatarMode.AVATAR)
						.withTooltipConfig(new TooltipConfig()));

				IModel<Repository> repoModel = new AbstractReadOnlyModel<Repository>() {

					@Override
					public Repository getObject() {
						return updateModel.getObject().getRequest().getTarget().getRepository();
					}
					
				};
				item.add(new CommitMessagePanel("message", repoModel, new AbstractReadOnlyModel<Commit>() {

					@Override
					public Commit getObject() {
						return item.getModelObject();
					}
					
				}));

				IModel<PullRequest> requestModel = new AbstractReadOnlyModel<PullRequest>() {

					@Override
					public PullRequest getObject() {
						return updateModel.getObject().getRequest();
					}
					
				};
				item.add(new VerificationStatusPanel("verification", requestModel, Model.of(commit.getHash())) {

					@Override
					protected Component newStatusComponent(String id, final IModel<Verification.Status> statusModel) {
						return new Label(id, new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (statusModel.getObject() == Verification.Status.PASSED)
									return "<i class='fa fa-check'></i><i class='caret'></i> ";
								else if (statusModel.getObject() == Verification.Status.ONGOING)
									return "<i class='fa fa-clock-o'></i><i class='caret'></i> ";
								else if (statusModel.getObject() == Verification.Status.NOT_PASSED) 
									return "<i class='fa fa-times'></i><i class='caret'></i>";
								else 
									return "";
							}
							
						}) {

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								
								if (statusModel.getObject() == Verification.Status.PASSED) {
									tag.put("class", "successful");
									tag.put("title", "Build is successful");
								} else if (statusModel.getObject() == Verification.Status.ONGOING) {
									tag.put("class", "running");
									tag.put("title", "Build is running");
								} else if (statusModel.getObject() == Verification.Status.NOT_PASSED) { 
									tag.put("class", "failed");
									tag.put("title", "Build is failed");
								}
							}

							@Override
							protected void onDetach() {
								statusModel.detach();
								
								super.onDetach();
							}
							
						}.setEscapeModelStrings(false);
					}

				});
				
				item.add(new CommitHashPanel("hash", Model.of(commit.getHash())));
				if (mergedCommitsModel.getObject().contains(commit.getHash())) {
					item.add(AttributeAppender.append("class", " integrated"));
					item.add(AttributeAppender.append("title", "This commit has been integrated"));
				} else if (!updateModel.getObject().getRequest().getPendingCommits().contains(commit.getHash())) {
					item.add(AttributeAppender.append("class", " rebased"));
					item.add(AttributeAppender.append("title", "This commit has been rebased"));
				}
				
				PullRequest request = updateModel.getObject().getRequest();
				HistoryState state = new HistoryState();
				state.requestId = request.getId();
				state.blobIdent.revision = commit.getHash();
				PageParameters params = RepoFilePage.paramsOf(request.getTargetRepo(), state);
				item.add(new BookmarkablePageLink<Void>("codeLink", RepoFilePage.class, params));
			}
			
		});
		
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		
		updateModel.detach();
		mergedCommitsModel.detach();
	}

}

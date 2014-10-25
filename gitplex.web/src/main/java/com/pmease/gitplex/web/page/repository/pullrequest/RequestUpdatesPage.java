package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequestVerification;
import com.pmease.gitplex.core.model.PullRequestVote;
import com.pmease.gitplex.web.component.commit.CommitHashLink;
import com.pmease.gitplex.web.component.commit.CommitMessagePanel;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class RequestUpdatesPage extends RequestDetailPage {

	public RequestUpdatesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer updatesContainer = new WebMarkupContainer("requestUpdatesContainer");
		updatesContainer.add(new PullRequestChangeBehavior(getPullRequest().getId()));
		add(updatesContainer);
		
		updatesContainer.add(new ListView<PullRequestUpdate>("updates", new LoadableDetachableModel<List<PullRequestUpdate>>() {

			@Override
			public List<PullRequestUpdate> load() {
				return getPullRequest().getSortedUpdates();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<PullRequestUpdate> updateItem) {
				PullRequestUpdate update = updateItem.getModelObject();

				List<PullRequestUpdate> updates = update.getRequest().getSortedUpdates();
				int updateNo = updates.indexOf(update) + 1;
				updateItem.add(new Label("updateNo", updateNo));
				PageParameters params = RequestComparePage.paramsOf(
						update.getRequest(), update.getBaseCommitHash(), update.getHeadCommitHash(), null);
				
				updateItem.add(new AgeLabel("age", Model.of(update.getDate())));
				
				Link<Void> compareLink = new BookmarkablePageLink<Void>("compare", RequestComparePage.class, params);
				if (updateNo == 1)
					compareLink.add(AttributeAppender.append("title", "Compare with request base"));
				else
					compareLink.add(AttributeAppender.append("title", "Compare with previous update"));
				updateItem.add(compareLink);

				updateItem.add(new ListView<PullRequestVote>("votes", new LoadableDetachableModel<List<PullRequestVote>>() {

					@Override
					protected List<PullRequestVote> load() {
						List<PullRequestVote> votes = new ArrayList<>(updateItem.getModelObject().getVotes());
						Collections.sort(votes, new Comparator<PullRequestVote>() {

							@Override
							public int compare(PullRequestVote vote1, PullRequestVote vote2) {
								return vote1.getDate().compareTo(vote2.getDate());
							}
							
						});
						return votes;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<PullRequestVote> item) {
						PullRequestVote vote = item.getModelObject();

						item.add(new UserLink("user", new UserModel(vote.getVoter()), AvatarMode.AVATAR)
									.withTooltipConfig(new TooltipConfig()));
						Label label;
						if (vote.getResult() == PullRequestVote.Result.APPROVE) {
							label = new Label("label", "Approved");
							label.add(AttributeModifier.append("class", " label-success"));
						} else {
							label = new Label("label", "Disapproved");
							label.add(AttributeModifier.append("class", " label-danger"));
						}
						item.add(label);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!updateItem.getModelObject().getVotes().isEmpty());
					}
					
				});

				final Set<String> mergedCommitHashes = new HashSet<>();

				for (Commit commit: update.getRequest().getMergedCommits())
					mergedCommitHashes.add(commit.getHash());

				updateItem.add(new ListView<Commit>("commits", new AbstractReadOnlyModel<List<Commit>>() {

					@Override
					public List<Commit> getObject() {
						return updateItem.getModelObject().getCommits();
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<Commit> commitItem) {
						Commit commit = commitItem.getModelObject();
						
						commitItem.add(new PersonLink("avatar", Model.of(commit.getAuthor()), AvatarMode.AVATAR));

						IModel<Repository> repoModel = new AbstractReadOnlyModel<Repository>() {

							@Override
							public Repository getObject() {
								return updateItem.getModelObject().getRequest().getTarget().getRepository();
							}
							
						};
						commitItem.add(new CommitMessagePanel("message", repoModel, new AbstractReadOnlyModel<Commit>() {

							@Override
							public Commit getObject() {
								return commitItem.getModelObject();
							}
							
						}));

						commitItem.add(new PersonLink("name", Model.of(commit.getAuthor()), AvatarMode.NAME));
						commitItem.add(new AgeLabel("age", Model.of(commit.getAuthor().getWhen())));
						
						commitItem.add(new CommitHashLink("hashLink", repoModel, commit.getHash()));
						commitItem.add(new BookmarkablePageLink<Void>("treeLink", RepoTreePage.class, 
								RepoTreePage.paramsOf(repoModel.getObject(), commit.getHash())));
						
						commitItem.add(new VerificationStatusPanel("verification", requestModel, Model.of(commit.getHash())) {

							@Override
							protected Component newStatusComponent(String id, final IModel<PullRequestVerification.Status> statusModel) {
								return new Label(id, new AbstractReadOnlyModel<String>() {

									@Override
									public String getObject() {
										if (statusModel.getObject() == PullRequestVerification.Status.PASSED)
											return "build successful <i class='caret'></i>";
										else if (statusModel.getObject() == PullRequestVerification.Status.ONGOING)
											return "build running <i class='caret'></i>";
										else if (statusModel.getObject() == PullRequestVerification.Status.NOT_PASSED) 
											return "build failed <i class='caret'></i>";
										else 
											return "";
									}
									
								}) {

									@Override
									protected void onComponentTag(ComponentTag tag) {
										super.onComponentTag(tag);
										
										if (statusModel.getObject() == PullRequestVerification.Status.PASSED)
											tag.put("class", "label label-success");
										else if (statusModel.getObject() == PullRequestVerification.Status.ONGOING)
											tag.put("class", "label label-warning");
										else if (statusModel.getObject() == PullRequestVerification.Status.NOT_PASSED) 
											tag.put("class", "label label-danger");
									}

									@Override
									protected void onDetach() {
										statusModel.detach();
										
										super.onDetach();
									}
									
								}.setEscapeModelStrings(false);
							}
							
						});

						if (mergedCommitHashes.contains(commit.getHash())) {
							commitItem.add(new Label("integration", "integrated")
									.add(AttributeAppender.append("class", "label label-success")));
							commitItem.add(AttributeAppender.append("class", " integrated"));
						} else if (!getPullRequest().getPendingCommits().contains(commit.getHash())) {
							commitItem.add(new Label("integration", "rebased")
									.add(AttributeAppender.append("class", "label label-default")));
							commitItem.add(AttributeAppender.append("class", " rebased"));
						} else {
							commitItem.add(new WebMarkupContainer("integration"));
						}
						
					}
					
				});
			}
			
		});		
	}

}

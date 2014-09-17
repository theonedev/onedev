package com.pmease.gitplex.web.page.repository.info.pullrequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
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
import com.pmease.gitplex.core.model.Verification;
import com.pmease.gitplex.core.model.Verification.Status;
import com.pmease.gitplex.core.model.Vote;
import com.pmease.gitplex.web.component.commit.CommitHashLink;
import com.pmease.gitplex.web.component.commit.CommitMessagePanel;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.info.code.tree.RepoTreePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class RequestUpdatesPage extends RequestDetailPage {

	public RequestUpdatesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<PullRequestUpdate>("updates", new LoadableDetachableModel<List<PullRequestUpdate>>() {

			@Override
			public List<PullRequestUpdate> load() {
				return getPullRequest().getSortedUpdates();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<PullRequestUpdate> updateItem) {
				PullRequestUpdate update = updateItem.getModelObject();

				updateItem.add(new UserLink("name", new UserModel(update.getUser()), AvatarMode.NAME));
				List<PullRequestUpdate> updates = update.getRequest().getSortedUpdates();
				int updateNo = updates.indexOf(update) + 1;
				updateItem.add(new Label("updateNo", updateNo));
				PageParameters params = RequestComparePage.paramsOf(
						update.getRequest(), update.getBaseCommit(), update.getHeadCommit(), null, null);
				
				updateItem.add(new AgeLabel("age", Model.of(update.getDate())));
				
				Link<Void> compareLink = new BookmarkablePageLink<Void>("compare", RequestComparePage.class, params);
				if (updateNo == 1)
					compareLink.add(AttributeAppender.append("title", "Compare with request base"));
				else
					compareLink.add(AttributeAppender.append("title", "Compare with previous update"));
				updateItem.add(compareLink);

				updateItem.add(new ListView<Vote>("votes", new LoadableDetachableModel<List<Vote>>() {

					@Override
					protected List<Vote> load() {
						List<Vote> votes = new ArrayList<>(updateItem.getModelObject().getVotes());
						Collections.sort(votes, new Comparator<Vote>() {

							@Override
							public int compare(Vote vote1, Vote vote2) {
								return vote1.getDate().compareTo(vote2.getDate());
							}
							
						});
						return votes;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<Vote> item) {
						Vote vote = item.getModelObject();

						item.add(new UserLink("user", new UserModel(vote.getVoter()), AvatarMode.AVATAR)
									.withTooltipConfig(new TooltipConfig()));
						Label label;
						if (vote.getResult() == Vote.Result.APPROVE) {
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

				for (Commit commit: update.getMergedCommits())
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
						
						commitItem.add(new VerificationStatusPanel("verification", requestModel, commit.getHash()) {

							@Override
							protected Component newStatusComponent(String id, Status status) {
								if (status == Verification.Status.PASSED) {
									return new Label(id, "build successful <i class='caret'></i>")
										.setEscapeModelStrings(false)
										.add(AttributeAppender.append("class", " label label-success"));
								} else if (status == Verification.Status.ONGOING) {
									return new Label(id, "build running <i class='caret'></i>")
										.setEscapeModelStrings(false)
										.add(AttributeAppender.append("class", " label label-warning"));
								} else {
									return new Label(id, "build failed <i class='caret'></i>")
										.setEscapeModelStrings(false)
										.add(AttributeAppender.append("class", " label label-danger"));
								} 
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

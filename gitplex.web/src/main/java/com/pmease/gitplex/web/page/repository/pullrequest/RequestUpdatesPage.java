package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
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
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.PullRequestVerification;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.avatar.RemoveableAvatar;
import com.pmease.gitplex.web.component.commitlink.CommitLink;
import com.pmease.gitplex.web.component.commitmessage.OldCommitMessagePanel;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.component.pullrequest.ReviewResultIcon;
import com.pmease.gitplex.web.event.PullRequestChanged;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.tree.RepoTreePage;

@SuppressWarnings("serial")
public class RequestUpdatesPage extends RequestDetailPage {

	public RequestUpdatesPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer updatesContainer = new WebMarkupContainer("requestUpdatesContainer") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PullRequestChanged) {
					PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
					pullRequestChanged.getTarget().add(this);
				}
			}
			
		};
		updatesContainer.setOutputMarkupId(true);
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

				final WebMarkupContainer reviewsContainer = new WebMarkupContainer("reviews");
				reviewsContainer.setOutputMarkupId(true);
				updateItem.add(reviewsContainer);
				reviewsContainer.add(new ListView<Review>("reviews", new LoadableDetachableModel<List<Review>>() {

					@Override
					protected List<Review> load() {
						List<Review> reviewsOfUpdate = new ArrayList<>();
						for (Review review: getPullRequest().getReviews()) {
							if (review.getUpdate().equals(updateItem.getModelObject()))
								reviewsOfUpdate.add(review);
						}
						return reviewsOfUpdate;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<Review> item) {
						final Review review = item.getModelObject();
						item.add(new RemoveableAvatar("avatar", new UserModel(review.getReviewer()), "Remove review by") {
							
							@Override
							protected void onAvatarRemove(AjaxRequestTarget target) {
								GitPlex.getInstance(ReviewManager.class).delete(review);
								target.add(reviewsContainer);
								send(getPage(), Broadcast.BREADTH, new PullRequestChanged(target, getPullRequest()));								
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								
								setEnabled(getPullRequest().isOpen() 
										&& (item.getModelObject().getReviewer().equals(getCurrentUser()) 
												|| SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepoAdmin(getRepository()))));
							}

						});
						item.add(new ReviewResultIcon("result", item.getModel()));
					}

				});

				final Set<String> mergedCommitHashes = new HashSet<>();

				for (Commit commit: update.getRequest().getMergedCommits())
					mergedCommitHashes.add(commit.getHash());

				updateItem.add(new ListView<Commit>("commits", new LoadableDetachableModel<List<Commit>>() {

					@Override
					protected List<Commit> load() {
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
						commitItem.add(new OldCommitMessagePanel("message", repoModel, new AbstractReadOnlyModel<Commit>() {

							@Override
							public Commit getObject() {
								return commitItem.getModelObject();
							}
							
						}));

						commitItem.add(new PersonLink("name", Model.of(commit.getAuthor()), AvatarMode.NAME));
						commitItem.add(new AgeLabel("age", Model.of(commit.getAuthor().getWhen())));
						
						commitItem.add(new CommitLink("hashLink", repoModel, commit.getHash()));
						commitItem.add(new BookmarkablePageLink<Void>("treeLink", RepoTreePage.class, 
								RepoTreePage.paramsOf(repoModel.getObject(), commit.getHash(), null)));
						
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

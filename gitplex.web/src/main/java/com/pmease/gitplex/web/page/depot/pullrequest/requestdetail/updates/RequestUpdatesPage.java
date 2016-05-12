package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.updates;

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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.Verification;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.security.ObjectPermission;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.ContributorAvatars;
import com.pmease.gitplex.web.component.avatar.RemoveableAvatar;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.contributorpanel.ContributorPanel;
import com.pmease.gitplex.web.component.hashandcode.HashAndCodePanel;
import com.pmease.gitplex.web.component.pullrequest.ReviewResultIcon;
import com.pmease.gitplex.web.component.pullrequest.verificationstatus.VerificationStatusPanel;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.UpdateChangesLink;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.util.DateUtils;
import com.pmease.gitplex.web.websocket.PullRequestChanged;

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
					pullRequestChanged.getPartialPageRequestHandler().add(this);
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
				updateItem.add(new Label("age", DateUtils.formatAge(update.getDate())));

				updateItem.add(new UpdateChangesLink("changes", update));

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
								send(getPage(), Broadcast.BREADTH, 
										new PullRequestChanged(target, getPullRequest(), PullRequest.Event.REVIEW_REMOVED));								
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								
								setEnabled(getPullRequest().isOpen() 
										&& (item.getModelObject().getReviewer().equals(getLoginUser()) 
												|| SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotAdmin(getDepot()))));
							}

						});
						item.add(new ReviewResultIcon("result", item.getModel()));
					}

				});

				final Set<String> mergedCommitHashes = new HashSet<>();

				for (Commit commit: update.getRequest().getMergedCommits())
					mergedCommitHashes.add(commit.getHash());

				String tooManyMessage = "Too many commits, displaying recent " + Constants.MAX_DISPLAY_COMMITS;
				updateItem.add(new Label("tooManyCommits", tooManyMessage) {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						List<Commit> commits = updateItem.getModelObject().getCommits();
						setVisible(commits.size()>Constants.MAX_DISPLAY_COMMITS);
					}
					
				});
				updateItem.add(new ListView<Commit>("commits", new LoadableDetachableModel<List<Commit>>() {

					@Override
					protected List<Commit> load() {
						List<Commit> commits = updateItem.getModelObject().getCommits();
						if (commits.size() > Constants.MAX_DISPLAY_COMMITS)
							return commits.subList(commits.size()-Constants.MAX_DISPLAY_COMMITS, commits.size());
						else 
							return commits;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<Commit> commitItem) {
						Commit commit = commitItem.getModelObject();
						
						commitItem.add(new ContributorAvatars("avatar", commit.getAuthor(), commit.getCommitter()));

						IModel<Depot> depotModel = new AbstractReadOnlyModel<Depot>() {

							@Override
							public Depot getObject() {
								return updateItem.getModelObject().getRequest().getTarget().getDepot();
							}
							
						};
						commitItem.add(new CommitMessagePanel("message", depotModel, new AbstractReadOnlyModel<Commit>() {

							@Override
							public Commit getObject() {
								return commitItem.getModelObject();
							}
							
						}));

						commitItem.add(new ContributorPanel("contribution", commit.getAuthor(), commit.getCommitter(), true));
						
						commitItem.add(new HashAndCodePanel("hashAndCode", new AbstractReadOnlyModel<Depot>() {

							@Override
							public Depot getObject() {
								return getPullRequest().getTargetDepot();
							}
							
						}, commit.getHash(), null, getPullRequest().getId()));

						commitItem.add(new VerificationStatusPanel("verification", requestModel, Model.of(commit.getHash())) {

							@Override
							protected Component newStatusComponent(String id, final IModel<Verification.Status> statusModel) {
								return new Label(id, new AbstractReadOnlyModel<String>() {

									@Override
									public String getObject() {
										if (statusModel.getObject() == Verification.Status.PASSED)
											return "build successful <i class='caret'></i>";
										else if (statusModel.getObject() == Verification.Status.ONGOING)
											return "build running <i class='caret'></i>";
										else if (statusModel.getObject() == Verification.Status.NOT_PASSED) 
											return "build failed <i class='caret'></i>";
										else 
											return "";
									}
									
								}) {

									@Override
									protected void onComponentTag(ComponentTag tag) {
										super.onComponentTag(tag);
										
										if (statusModel.getObject() == Verification.Status.PASSED)
											tag.put("class", "label label-success");
										else if (statusModel.getObject() == Verification.Status.ONGOING)
											tag.put("class", "label label-warning");
										else if (statusModel.getObject() == Verification.Status.NOT_PASSED) 
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

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RequestUpdatesPage.class, "request-updates.css")));
	}
	
}

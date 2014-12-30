package com.pmease.gitplex.web.page.repository.pullrequest;

import static com.pmease.gitplex.core.model.PullRequestOperation.APPROVE;
import static com.pmease.gitplex.core.model.PullRequestOperation.DISAPPROVE;
import static com.pmease.gitplex.core.model.PullRequestOperation.DISCARD;
import static com.pmease.gitplex.core.model.PullRequestOperation.INTEGRATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.behavior.AllowLeaveBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.component.markdown.MarkdownInput;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequestAudit;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.PullRequestVerification;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.comment.event.PullRequestChanged;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.pullrequest.AssigneeChoice;
import com.pmease.gitplex.web.component.pullrequest.ReviewResultIcon;
import com.pmease.gitplex.web.component.pullrequest.ReviewerAvatar;
import com.pmease.gitplex.web.component.pullrequest.ReviewerChoice;
import com.pmease.gitplex.web.component.user.AvatarByUser;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.model.ReviewersModel;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.ApprovePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.CommentPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.DisapprovePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.DiscardPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.IntegratePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.OpenPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.PullRequestActivity;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.UpdatePullRequest;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RequestOverviewPage extends RequestDetailPage {
	
	private static final String ASSIGNEE_HELP = "Assignee is resonsible for integrating "
			+ "the pull request into target branch after it passes gate keeper check.";

	private RepeatingView activitiesView;
	
	public RequestOverviewPage(PageParameters params) {
		super(params);
	}
	
	private Component newActivityRow(final String id, PullRequestActivity activity) {
		final CommentPullRequest commentActivity;
		if (activity instanceof CommentPullRequest)
			commentActivity = (CommentPullRequest) activity;
		else
			commentActivity = null;
		
		final WebMarkupContainer row = new WebMarkupContainer(id, Model.of(activity)) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof CommentRemoved) {
					CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
					remove();
					commentRemoved.getTarget().appendJavaScript(String.format("$('#%s').remove();", getMarkupId()));
				} else if (event.getPayload() instanceof CommentCollapsing) {
					Preconditions.checkNotNull(commentActivity);
					commentActivity.setCollapsed(true);
					Component row = newActivityRow(id, commentActivity);
					replaceWith(row);
					((CommentCollapsing) event.getPayload()).getTarget().add(row);
				}
			}
			
		};
		if (commentActivity != null && commentActivity.isCollapsed()) {
			PullRequestComment comment = commentActivity.getComment();

			Fragment fragment = new Fragment("activity", "collapsedCommentFrag", RequestOverviewPage.this);

			fragment.add(new UserLink("user", new UserModel(comment.getUser()), AvatarMode.NAME));
			if (comment.getInlineInfo() != null)
				fragment.add(new Label("activity", "added inline comment on file '" + comment.getBlobInfo().getPath() + "'"));
			else 
				fragment.add(new Label("activity", "commented"));
			fragment.add(new AgeLabel("age", Model.of(comment.getDate())));
			
			fragment.add(new Label("detail", comment.getContent()));
			
			fragment.add(new AjaxLink<Void>("expand") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					PullRequestActivity activity = (PullRequestActivity) row.getDefaultModelObject();
					row.replace(activity.render("activity"));
					commentActivity.setCollapsed(false);
					target.add(row);
				}
				
			});
			row.add(fragment);
		}

		row.setOutputMarkupId(true);
		
		row.add(new UserLink("avatar", new UserModel(activity.getUser()), AvatarMode.AVATAR));
		
		if (row.get("activity") == null) 
			row.add(activity.render("activity"));
		
		if (activity instanceof OpenPullRequest || activity instanceof CommentPullRequest)
			row.add(AttributeAppender.append("class", " discussion non-update"));
		else if (activity instanceof UpdatePullRequest)
			row.add(AttributeAppender.append("class", " non-discussion update"));
		else
			row.add(AttributeAppender.append("class", " non-discussion non-update"));
		
		row.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String cssClasses = "";
				PullRequestActivity activity = (PullRequestActivity) row.getDefaultModelObject();
				if (activity instanceof CommentPullRequest) {
					CommentPullRequest commentActivity = (CommentPullRequest) activity;
					if (commentActivity.isCollapsed())
						cssClasses += " collapsed";
					if (commentActivity.getComment().isResolved())
						cssClasses += " resolved";
				} 
				return cssClasses;
			}
			
		}));
		
		return row;
	}
	
	private List<PullRequestActivity> getActivities() {
		PullRequest request = getPullRequest();
		List<PullRequestActivity> activities = new ArrayList<>();

		activities.add(new OpenPullRequest(request));

		for (PullRequestUpdate update: request.getUpdates())
			activities.add(new UpdatePullRequest(update));
		
		for (PullRequestComment comment: request.getComments()) 
			activities.add(new CommentPullRequest(comment));
		
		for (PullRequestAudit audit: request.getAudits()) {
			if (audit.getOperation() == INTEGRATE) {
				activities.add(new IntegratePullRequest(audit.getUser(), audit.getDate()));
			} else if (audit.getOperation() == DISCARD) { 
				activities.add(new DiscardPullRequest(audit.getUser(), audit.getDate()));
			} else if (audit.getOperation() == APPROVE) {
				activities.add(new ApprovePullRequest(audit.getRequest(), audit.getUser(), audit.getDate()));
			} else if (audit.getOperation() == DISAPPROVE) {
				activities.add(new DisapprovePullRequest(audit.getRequest(), audit.getUser(), audit.getDate()));
			} else {
				throw new IllegalStateException("Unexpected audit operation: " + audit.getOperation());
			}
		}
		
		Collections.sort(activities, new Comparator<PullRequestActivity>() {

			@Override
			public int compare(PullRequestActivity o1, PullRequestActivity o2) {
				if (o1.getDate().before(o2.getDate()))
					return -1;
				else if (o1.getDate().after(o2.getDate()))
					return 1;
				else if (o1 instanceof OpenPullRequest || o1 instanceof CommentPullRequest)
					return -1;
				else
					return 1;
			}
			
		});
		
		return activities;
	}
	
	private Component newActivitiesView() {
		activitiesView = new RepeatingView("requestActivities");
		activitiesView.setOutputMarkupId(true);
		
		List<PullRequestActivity> activities = getActivities();
		
		for (PullRequestActivity activity: activities) 
			activitiesView.add(newActivityRow(activitiesView.newChildId(), activity));
		
		return activitiesView;
	}
	
	@Override
	protected void onBeforeRender() {
		replace(newActivitiesView());
		
		super.onBeforeRender();
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PullRequestChanged) {
			PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
			AjaxRequestTarget target = pullRequestChanged.getTarget();
			List<PullRequestActivity> activities = getActivities();
			Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
			PullRequestActivity lastAcvitity = (PullRequestActivity) lastActivityRow.getDefaultModelObject();
			for (PullRequestActivity activity: activities) {
				if (activity.getDate().after(lastAcvitity.getDate())) {
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), activity); 
					activitiesView.add(newActivityRow);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newActivityRow.getMarkupId(), lastActivityRow.getMarkupId());
					target.prependJavaScript(script);
					target.add(newActivityRow);
					lastActivityRow = newActivityRow;
				}
			}
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newActivitiesView());
		
		final WebMarkupContainer addComment = new WebMarkupContainer("addComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance(UserManager.class).getCurrent() != null);
			}
			
		};
		addComment.setOutputMarkupId(true);
		add(addComment);
		
		Form<?> form = new Form<Void>("form");
		addComment.add(form);
		
		final MarkdownInput input = new MarkdownInput("input", Model.of(""));
		input.setRequired(true);
		form.add(input);
		
		form.add(new AjaxSubmitLink("comment") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				PullRequestComment comment = new PullRequestComment();
				comment.setRequest(getPullRequest());
				comment.setUser(GitPlex.getInstance(UserManager.class).getCurrent());
				comment.setContent(input.getModelObject());
				InheritableThreadLocalData.set(new PageId(getPage().getPageId()));
				try {
					GitPlex.getInstance(PullRequestCommentManager.class).save(comment);
				} finally {
					InheritableThreadLocalData.clear();
				}
				input.setModelObject("");
				
				target.add(addComment);
				
				Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
				Component newActivityRow = newActivityRow(activitiesView.newChildId(), new CommentPullRequest(comment)); 
				activitiesView.add(newActivityRow);
				
				String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
						newActivityRow.getMarkupId(), lastActivityRow.getMarkupId());
				target.prependJavaScript(script);
				target.add(newActivityRow);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		}.add(new AllowLeaveBehavior()));
		
		add(newIntegrationContainer());
		add(newAssigneeContainer());
		add(newReviewersContainer());
	}

	private WebMarkupContainer newIntegrationContainer() {
		final WebMarkupContainer integrationContainer = new WebMarkupContainer("integration") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen());
			}
			
		};
		integrationContainer.setOutputMarkupId(true);

		if (getPullRequest().isOpen()) {
			integrationContainer.add(new WebSocketRenderBehavior(true) {
	
				@Override
				protected Object getTrait() {
					IntegrationPreviewUpdateTrait trait = new IntegrationPreviewUpdateTrait();
					trait.requestId = getPullRequest().getId();
					return trait;
				}
				
			});
		}

		PullRequest request = getPullRequest();
		
		final List<IntegrationStrategy> strategies = 
				GitPlex.getInstance(PullRequestManager.class).getApplicableIntegrationStrategies(request);
		if (!strategies.contains(request.getIntegrationStrategy())) {
			request.setIntegrationStrategy(strategies.get(0));
			GitPlex.getInstance(Dao.class).persist(request);
		}
		IModel<IntegrationStrategy> strategyModel = new IModel<IntegrationStrategy>() {

			@Override
			public void detach() {
			}

			@Override
			public IntegrationStrategy getObject() {
				return getPullRequest().getIntegrationStrategy();
			}

			@Override
			public void setObject(IntegrationStrategy object) {
				getPullRequest().setIntegrationStrategy(object);
			}
			
		};
		
		DropDownChoice<IntegrationStrategy> strategySelect = 
				new DropDownChoice<IntegrationStrategy>("strategySelect", strategyModel, strategies) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());
				setVisible(SecurityUtils.getSubject().isPermitted(writePermission) && strategies.size() > 1);						
			}
			
		};
		strategySelect.add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				GitPlex.getInstance(Dao.class).persist(getPullRequest());
				target.add(summaryContainer);
				target.add(integrationContainer);
			}
			
		});
		integrationContainer.add(strategySelect);
		
		integrationContainer.add(new Label("strategyLabel", request.getIntegrationStrategy()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());
				setVisible(!SecurityUtils.getSubject().isPermitted(writePermission) || strategies.size() == 1);						
			}
			
		});

		ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());

		if (!SecurityUtils.getSubject().isPermitted(writePermission) || strategies.size() == 1) {
			integrationContainer.add(new WebMarkupContainer("strategyHelp").add(
					new TooltipBehavior(Model.of(getPullRequest().getIntegrationStrategy().getDescription()))));
		} else {
			StringBuilder strategyHelp = new StringBuilder("<dl class='integration-strategy-help'>");
			
			for (IntegrationStrategy strategy: strategies) {
				strategyHelp.append("<dt>").append(strategy.toString()).append("</dt>");
				strategyHelp.append("<dd>").append(strategy.getDescription()).append("</dd>");
			}

			strategyHelp.append("</dl>");
			
			integrationContainer.add(new WebMarkupContainer("strategyHelp")
						.add(AttributeAppender.append("data-html", "true"))
						.add(new TooltipBehavior(Model.of(strategyHelp.toString()), new TooltipConfig().withPlacement(Placement.left))));
		}

		integrationContainer.add(new WebMarkupContainer("calculating") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getIntegrationPreview() == null);
			}
			
		});
		integrationContainer.add(new WebMarkupContainer("conflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				DropdownPanel resolveInstructions = new DropdownPanel("resolveInstructions", true) {

					@Override
					protected Component newContent(String id) {
						return new ResolveConflictInstructionPanel(id, new EntityModel<PullRequest>(getPullRequest()));
					}
					
				};
				add(resolveInstructions);
				WebMarkupContainer resolveInstructionsTrigger = new WebMarkupContainer("resolveInstructionsTrigger") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().getSource() != null);
					}
					
				};
				resolveInstructionsTrigger.add(new DropdownBehavior(resolveInstructions));
				add(resolveInstructionsTrigger);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				IntegrationPreview preview = getPullRequest().getIntegrationPreview();
				setVisible(preview != null && preview.getIntegrated() == null);
			}

		});
		integrationContainer.add(new WebMarkupContainer("noConflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();

				PullRequest request = getPullRequest();
				IntegrationPreview preview = request.getIntegrationPreview();
				
				PageParameters params = RequestComparePage.paramsOf(
						request, request.getTarget().getHeadCommitHash(), 
						preview!=null?preview.getIntegrated():null, null);
				
				Link<Void> link = new BookmarkablePageLink<Void>("preview", RequestComparePage.class, params) {
					
					@Override
					protected void onConfigure() {
						super.onConfigure();

						PullRequest request = getPullRequest();
						IntegrationPreview preview = request.getIntegrationPreview();
						setVisible(!preview.getIntegrated().equals(preview.getRequestHead()));
					}
					
				};
				add(link);

				add(new VerificationStatusPanel("verification", requestModel, new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						IntegrationPreview preview = request.getIntegrationPreview();
						if (preview != null)
							return preview.getIntegrated();
						else
							return null;
					}
					
				}) {

					@Override
					protected Component newStatusComponent(String id, final IModel<PullRequestVerification.Status> statusModel) {
						return new Label(id, new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								if (statusModel.getObject() == PullRequestVerification.Status.PASSED)
									return "successful <i class='caret'></i>";
								else if (statusModel.getObject() == PullRequestVerification.Status.ONGOING)
									return "running <i class='caret'></i>";
								else if (statusModel.getObject() == PullRequestVerification.Status.NOT_PASSED) 
									return "failed <i class='caret'></i>";
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
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				IntegrationPreview preview = getPullRequest().getIntegrationPreview();
				setVisible(preview != null && preview.getIntegrated() != null);
			}

		});
		
		return integrationContainer;
	}
	
	private Component newAssigneeContainer() {
		Fragment assigneeContainer;
		PullRequest request = getPullRequest();
		User assignee = request.getAssignee();
		boolean canChangeAssignee = request.isOpen() 
				&& (request.getSubmitter().equals(getCurrentUser()) 
					|| SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(getRepository())));
		if (assignee != null) {
			
			if (canChangeAssignee) {
				assigneeContainer = new Fragment("assignee", "assigneeEditFrag", this);			
				assigneeContainer.add(new WebMarkupContainer("help").add(new TooltipBehavior(Model.of(ASSIGNEE_HELP))));
				
				AssigneeChoice choice = new AssigneeChoice("assignee", repoModel, new IModel<User>() {

					@Override
					public void detach() {
					}

					@Override
					public User getObject() {
						return getPullRequest().getAssignee();
					}

					@Override
					public void setObject(User object) {
						getPullRequest().setAssignee(object);
					}
					
				});
				choice.setRequired(true);
				assigneeContainer.add(choice);
				choice.add(new AjaxFormComponentUpdatingBehavior("change") {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						Preconditions.checkNotNull(getPullRequest().getAssignee());
						GitPlex.getInstance(PullRequestManager.class).onAssigneeChange(getPullRequest());
					}
					
				});
			} else {
				assigneeContainer = new Fragment("assignee", "assigneeViewFrag", this);
				assigneeContainer.add(new WebMarkupContainer("help").add(new TooltipBehavior(Model.of(ASSIGNEE_HELP))));
				assigneeContainer.add(new AvatarByUser("assignee", new UserModel(assignee), true));
			}
		} else {
			assigneeContainer = new Fragment("assignee", "noAssigneeFrag", this);
		}
		
		return assigneeContainer;
	}
	
	private WebMarkupContainer newReviewersContainer() {
		final WebMarkupContainer reviewersContainer = new WebMarkupContainer("reviewers");
		reviewersContainer.setOutputMarkupId(true);
		reviewersContainer.add(new ListView<ReviewInvitation>("reviewers", new ReviewersModel(requestModel)) {

			@Override
			protected void populateItem(ListItem<ReviewInvitation> item) {
				ReviewInvitation invitation = item.getModelObject();
				item.add(new ReviewerAvatar("avatar", invitation) {

					@Override
					protected void onAvatarRemove(AjaxRequestTarget target) {
						super.onAvatarRemove(target);
						
						target.add(reviewersContainer);
					}
					
				});

				List<Review> userReviews = new ArrayList<>();
				for (Review review: getPullRequest().getReviews()) {
					if (review.getReviewer().equals(invitation.getReviewer()))
						userReviews.add(review);
				}
				if (!userReviews.isEmpty()) {
					Review review = userReviews.get(userReviews.size()-1); 
					item.add(new ReviewResultIcon("result", new EntityModel<Review>(review)));
				} else {
					item.add(new WebMarkupContainer("result").setVisible(false));
				}
			}
			
		});
		
		reviewersContainer.add(new ReviewerChoice("addReviewer", requestModel) {

			@Override
			protected void onSelect(AjaxRequestTarget target, User user) {
				super.onSelect(target, user);
				
				target.add(reviewersContainer);
			}
			
		});
		
		return reviewersContainer;
	}
	
}

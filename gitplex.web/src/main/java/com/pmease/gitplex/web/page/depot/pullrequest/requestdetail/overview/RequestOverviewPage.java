package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.entity.support.PullRequestEvent;
import com.pmease.gitplex.core.entity.PullRequestActivity;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestReference;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.PullRequestWatch;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.security.ObjectPermission;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.component.pullrequest.ReviewResultIcon;
import com.pmease.gitplex.web.component.pullrequest.requestassignee.AssigneeChoice;
import com.pmease.gitplex.web.component.pullrequest.requestreviewer.ReviewerAvatar;
import com.pmease.gitplex.web.component.pullrequest.requestreviewer.ReviewerChoice;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.model.ReviewersModel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.ApprovedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.CommentRemoved;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.CommentedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.DisapprovedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.DiscardedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.IntegratedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.OpenedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.ReferencedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.ReopenedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.SourceBranchDeletedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.SourceBranchRestoredRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity.UpdatedRenderer;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.websocket.PullRequestChanged;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RequestOverviewPage extends RequestDetailPage {
	
	private static final String ASSIGNEE_HELP = "Assignee has write permission to the "
			+ "repository and is resonsible for integrating the pull request into "
			+ "target branch after it passes gate keeper check.";

	private enum WatchStatus {
		WATCHING("Watching"), 
		NOT_WATCHING("Not Watching"), 
		IGNORE("Ignore");
		
		private final String displayName;
		
		WatchStatus(String displayName) {
			this.displayName = displayName;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
		
	};
	
	private RepeatingView activitiesView;
	
	public RequestOverviewPage(PageParameters params) {
		super(params);
	}
	
	private Component newActivityRow(final String id, ActivityRenderer renderer) {
		WebMarkupContainer row = new WebMarkupContainer(id, Model.of(renderer)) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof CommentRemoved) {
					CommentRemoved commentRemoved = (CommentRemoved) event.getPayload();
					remove();
					commentRemoved.getPartialPageRequestHandler().appendJavaScript(String.format("$('#%s').remove();", getMarkupId()));
				} 
			}
			
		};

		row.setOutputMarkupId(true);
		
		WebMarkupContainer avatarColumn = new WebMarkupContainer("avatar");
		avatarColumn.add(new AvatarLink("avatar", renderer.getUser(), null));
		row.add(avatarColumn);
		
		if (row.get("content") == null) 
			row.add(renderer.render("content"));
		
		if (renderer instanceof OpenedRenderer || renderer instanceof CommentedRenderer)
			row.add(AttributeAppender.append("class", " discussion"));
		else
			row.add(AttributeAppender.append("class", " non-discussion"));
		
		if (renderer instanceof UpdatedRenderer)
			row.add(AttributeAppender.append("class", " update"));
		else
			row.add(AttributeAppender.append("class", " non-update"));

		if (!getPullRequest().isVisitedAfter(renderer.getDate()))
			row.add(AttributeAppender.append("class", "new"));
		
		return row;
	}
	
	private List<ActivityRenderer> getActivityRenders() {
		PullRequest request = getPullRequest();
		List<ActivityRenderer> renders = new ArrayList<>();

		renders.add(new OpenedRenderer(request));
		
		for (PullRequestUpdate update: request.getUpdates())
			renders.add(new UpdatedRenderer(update));
		
		for (PullRequestComment comment: request.getComments()) { 
			renders.add(new CommentedRenderer(comment));
		}
		
		for (PullRequestReference reference: request.getReferencedBy()) {
			renders.add(new ReferencedRenderer(request, reference.getUser(), 
					reference.getDate(), reference.getReferencedBy()));
		}
		
		for (PullRequestActivity activity: request.getActivities()) {
			if (activity.getEvent() == PullRequestEvent.INTEGRATED) {
				renders.add(new IntegratedRenderer(activity));
			} else if (activity.getEvent() == PullRequestEvent.DISCARDED) { 
				renders.add(new DiscardedRenderer(activity));
			} else if (activity.getEvent() == PullRequestEvent.APPROVED) {
				renders.add(new ApprovedRenderer(activity));
			} else if (activity.getEvent() == PullRequestEvent.DISAPPROVED) {
				renders.add(new DisapprovedRenderer(activity));
			} else if (activity.getEvent() == PullRequestEvent.REOPENED) {
				renders.add(new ReopenedRenderer(activity));
			} else if (activity.getEvent() == PullRequestEvent.SOURCE_BRANCH_DELETED) {
				renders.add(new SourceBranchDeletedRenderer(activity));
			} else if (activity.getEvent() == PullRequestEvent.SOURCE_BRANCH_RESTORED) {
				renders.add(new SourceBranchRestoredRenderer(activity));
			} else {
				throw new IllegalStateException("Unexpected event: " + activity.getEvent());
			}
		}
		
		renders.sort((o1, o2) -> {
			if (o1.getDate().before(o2.getDate()))
				return -1;
			else if (o1.getDate().after(o2.getDate()))
				return 1;
			else if (o1 instanceof OpenedRenderer)
				return -1;
			else
				return 1;
		});
		
		return renders;
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PullRequestChanged) {
			PullRequestChanged pullRequestChanged = (PullRequestChanged) event.getPayload();
			IPartialPageRequestHandler partialPageRequestHandler = pullRequestChanged.getPartialPageRequestHandler();
			List<ActivityRenderer> activities = getActivityRenders();

			@SuppressWarnings("deprecation")
			Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
			ActivityRenderer lastAcvitity = (ActivityRenderer) lastActivityRow.getDefaultModelObject();
			for (ActivityRenderer activity: activities) {
				if (activity.getDate().after(lastAcvitity.getDate())) {
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), activity); 
					activitiesView.add(newActivityRow);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newActivityRow.getMarkupId(), lastActivityRow.getMarkupId());
					partialPageRequestHandler.prependJavaScript(script);
					partialPageRequestHandler.add(newActivityRow);
					lastActivityRow = newActivityRow;
				}
			}
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(activitiesView = new RepeatingView("requestActivities"));
		activitiesView.setOutputMarkupId(true);
		
		List<ActivityRenderer> activities = getActivityRenders();
		
		for (ActivityRenderer activity: activities) 
			activitiesView.add(newActivityRow(activitiesView.newChildId(), activity));
		
		WebMarkupContainer addComment = new WebMarkupContainer("addComment") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null);
			}
			
		};
		addComment.setOutputMarkupId(true);
		add(addComment);
		
		Form<?> form = new Form<Void>("form");
		addComment.add(form);
		
		CommentInput input = new CommentInput("input", Model.of("")) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new DepotAttachmentSupport(getDepot(), getPullRequest().getUUID());
			}

			@Override
			protected Depot getDepot() {
				return RequestOverviewPage.this.getDepot();
			}
			
		};
		input.setRequired(true);
		form.add(input);
		
		form.add(new NotificationPanel("feedback", input));
		
		form.add(new AjaxSubmitLink("comment") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				PullRequestComment comment = new PullRequestComment();
				comment.setRequest(getPullRequest());
				comment.setUser(getLoginUser());
				comment.setContent(input.getModelObject());
				GitPlex.getInstance(PullRequestCommentManager.class).save(comment);
				input.setModelObject("");

				target.add(addComment);
				
				@SuppressWarnings("deprecation")
				Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
				Component newActivityRow = newActivityRow(activitiesView.newChildId(), new CommentedRenderer(comment)); 
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

		});
		
		add(newIntegrationStrategyContainer());
		add(newAssigneeContainer());
		add(newReviewersContainer());
		add(newWatchContainer());
	}

	private WebMarkupContainer newIntegrationStrategyContainer() {
		WebMarkupContainer integrationStrategyContainer = new WebMarkupContainer("integrationStrategy") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen());
			}
			
		};

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
		
		DropDownChoice<IntegrationStrategy> editor = 
				new DropDownChoice<IntegrationStrategy>("editor", strategyModel, strategies) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectPermission writePermission = ObjectPermission.ofDepotWrite(getDepot());
				setVisible(SecurityUtils.getSubject().isPermitted(writePermission) && strategies.size() > 1);						
			}
			
		};
		editor.add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				GitPlex.getInstance(Dao.class).persist(getPullRequest());
				send(getPage(), Broadcast.BREADTH, new PullRequestChanged(target, getPullRequest()));								
			}
			
		});
		integrationStrategyContainer.add(editor);
		
		integrationStrategyContainer.add(new Label("viewer", request.getIntegrationStrategy()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectPermission writePermission = ObjectPermission.ofDepotWrite(getDepot());
				setVisible(!SecurityUtils.getSubject().isPermitted(writePermission) || strategies.size() == 1);						
			}
			
		});

		ObjectPermission writePermission = ObjectPermission.ofDepotWrite(getDepot());

		if (!SecurityUtils.getSubject().isPermitted(writePermission) || strategies.size() == 1) {
			integrationStrategyContainer.add(new WebMarkupContainer("help").add(
					new TooltipBehavior(Model.of(getPullRequest().getIntegrationStrategy().getDescription()))));
		} else {
			StringBuilder strategyHelp = new StringBuilder("<dl class='integration-strategy-help'>");
			
			for (IntegrationStrategy strategy: strategies) {
				strategyHelp.append("<dt>").append(strategy.toString()).append("</dt>");
				strategyHelp.append("<dd>").append(strategy.getDescription()).append("</dd>");
			}

			strategyHelp.append("</dl>");
			
			integrationStrategyContainer.add(new WebMarkupContainer("help")
						.add(AttributeAppender.append("data-html", "true"))
						.add(new TooltipBehavior(Model.of(strategyHelp.toString()), new TooltipConfig().withPlacement(Placement.left))));
		}
		
		return integrationStrategyContainer;
	}
	
	private WebMarkupContainer newWatchContainer() {
		final WebMarkupContainer watchContainer = new WebMarkupContainer("watch") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null);
			}

		};
		
		watchContainer.setOutputMarkupId(true);

		final IModel<WatchStatus> optionModel = new LoadableDetachableModel<WatchStatus>() {

			@Override
			protected WatchStatus load() {
				PullRequestWatch watch = getPullRequest().getWatch(getLoginUser());
				if (watch != null) {
					if (watch.isIgnore())
						return WatchStatus.IGNORE;
					else
						return WatchStatus.WATCHING;
				} else {
					return WatchStatus.NOT_WATCHING;
				}
			}
			
		};
		
		List<WatchStatus> options = Arrays.asList(WatchStatus.values());
		
		IChoiceRenderer<WatchStatus> choiceRenderer = new IChoiceRenderer<WatchStatus>() {

			@Override
			public Object getDisplayValue(WatchStatus object) {
				return object.toString();
			}

			@Override
			public String getIdValue(WatchStatus object, int index) {
				return object.name();
			}

			@Override
			public WatchStatus getObject(String id, IModel<? extends List<? extends WatchStatus>> choices) {
				return WatchStatus.valueOf(id);
			}
			
		};
		DropDownChoice<WatchStatus> choice = new DropDownChoice<>("option", optionModel, 
				options, choiceRenderer);
		
		choice.add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				PullRequestWatch watch = getPullRequest().getWatch(getLoginUser());
				Dao dao = GitPlex.getInstance(Dao.class);
				if (optionModel.getObject() == WatchStatus.WATCHING) {
					if (watch != null) {
						watch.setIgnore(false);
						watch.setReason(null);
					} else {
						watch = new PullRequestWatch();
						watch.setRequest(getPullRequest());
						watch.setUser(getLoginUser());
						getPullRequest().getWatches().add(watch);
					}
					dao.persist(watch);
				} else if (optionModel.getObject() == WatchStatus.NOT_WATCHING) {
					if (watch != null) {
						dao.remove(watch);
						getPullRequest().getWatches().remove(watch);
					}
				} else {
					if (watch != null) {
						watch.setIgnore(true);
					} else {
						watch = new PullRequestWatch();
						watch.setRequest(getPullRequest());
						watch.setUser(getLoginUser());
						watch.setIgnore(true);
						getPullRequest().getWatches().add(watch);
					}
					dao.persist(watch);
				}
				target.add(watchContainer);
			}
			
		});
		watchContainer.add(choice);
		watchContainer.add(new Label("help", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequestWatch watch = getPullRequest().getWatch(getLoginUser());
				if (watch != null) {
					if (watch.isIgnore()) {
						return "Ignore notifications irrelevant to me.";
					} else if (watch.getReason() != null) {
						return watch.getReason();
					} else {
						return "You will be notified of any activities.";
					}
				} else {
					return "Ignore notifications irrelevant to me, but start to watch once I am involved."; 
				}
			}
			
		}));
		
		return watchContainer;
	}

	private Component newAssigneeContainer() {
		PullRequest request = getPullRequest();
		
		Fragment assigneeContainer;
		Account assignee = request.getAssignee();
		Account currentUser = getLoginUser();
		boolean canChangeAssignee = request.isOpen() 
				&& (currentUser != null && currentUser.equals(request.getSubmitter()) 
					|| SecurityUtils.canManage(getDepot()));
		if (assignee != null) {
			
			if (canChangeAssignee) {
				assigneeContainer = new Fragment("assignee", "assigneeEditFrag", this);			
				assigneeContainer.add(new WebMarkupContainer("help").add(new TooltipBehavior(Model.of(ASSIGNEE_HELP))));
				
				AssigneeChoice choice = new AssigneeChoice("assignee", depotModel, new IModel<Account>() {

					@Override
					public void detach() {
					}

					@Override
					public Account getObject() {
						return getPullRequest().getAssignee();
					}

					@Override
					public void setObject(Account object) {
						getPullRequest().setAssignee(object);
					}
					
				});
				choice.setRequired(true);
				assigneeContainer.add(choice);
				choice.add(new AjaxFormComponentUpdatingBehavior("change") {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						Preconditions.checkNotNull(getPullRequest().getAssignee());
						GitPlex.getInstance(PullRequestManager.class).changeAssignee(getPullRequest());
					}
					
				});
			} else {
				assigneeContainer = new Fragment("assignee", "assigneeViewFrag", this);
				assigneeContainer.add(new WebMarkupContainer("help").add(new TooltipBehavior(Model.of(ASSIGNEE_HELP))));
				assigneeContainer.add(new Avatar("assignee", assignee, new TooltipConfig()));
			}
		} else {
			assigneeContainer = new Fragment("assignee", "noAssigneeFrag", this);
		}
		
		return assigneeContainer;
	}
	
	private WebMarkupContainer newReviewersContainer() {
		final WebMarkupContainer reviewersContainer = new WebMarkupContainer("reviewers") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				if (request.getReviewInvitations().isEmpty()) {
					Account currentUser = GitPlex.getInstance(AccountManager.class).getCurrent();
					setVisible(request.isOpen() 
							&& !request.getPotentialReviewers().isEmpty()
							&& (currentUser != null && currentUser.equals(request.getSubmitter()) || SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotAdmin(request.getTarget().getDepot()))));
				} else {
					setVisible(true);
				}
			}
			
		};
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
						send(getPage(), Broadcast.BREADTH, new PullRequestChanged(target, getPullRequest()));								
					}
					
				});

				List<Review> userReviews = new ArrayList<>();
				for (Review review: getPullRequest().getReviews()) {
					if (review.getUser().equals(invitation.getUser()))
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
			protected void onSelect(AjaxRequestTarget target, Account user) {
				super.onSelect(target, user);
				
				target.add(reviewersContainer);
				send(getPage(), Broadcast.BREADTH, new PullRequestChanged(target, getPullRequest()));								
			}
			
		});
		
		return reviewersContainer;
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RequestOverviewPage.class, "request-overview.css")));
	}
	
}

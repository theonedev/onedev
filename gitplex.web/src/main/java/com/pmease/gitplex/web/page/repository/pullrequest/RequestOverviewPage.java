package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
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
import com.pmease.commons.wicket.component.markdown2.MarkdownInput;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.model.PullRequestActivity;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.PullRequestVisit;
import com.pmease.gitplex.core.model.PullRequestWatch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.comment.event.CommentCollapsing;
import com.pmease.gitplex.web.component.comment.event.CommentRemoved;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.pullrequest.AssigneeChoice;
import com.pmease.gitplex.web.component.pullrequest.ReviewResultIcon;
import com.pmease.gitplex.web.component.pullrequest.ReviewerAvatar;
import com.pmease.gitplex.web.component.pullrequest.ReviewerChoice;
import com.pmease.gitplex.web.component.user.AvatarByUser;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.event.PullRequestChanged;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.model.ReviewersModel;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.ApprovePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.CommentPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.DisapprovePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.DiscardPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.IntegratePullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.OpenPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.RenderableActivity;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.ReopenPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.UndoReviewPullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.activity.UpdatePullRequest;

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
	
	private Component newActivityRow(final String id, RenderableActivity activity) {
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
					RenderableActivity activity = (RenderableActivity) row.getDefaultModelObject();
					row.replace(activity.render("activity"));
					commentActivity.setCollapsed(false);
					target.add(row);
				}
				
			});
			row.add(fragment);
		}

		row.setOutputMarkupId(true);
		
		WebMarkupContainer avatarColumn = new WebMarkupContainer("avatar");
		avatarColumn.add(new UserLink("avatar", new UserModel(activity.getUser()), AvatarMode.AVATAR));
		row.add(avatarColumn);
		
		if (row.get("activity") == null) 
			row.add(activity.render("activity"));
		
		if (activity instanceof OpenPullRequest || activity instanceof CommentPullRequest)
			row.add(AttributeAppender.append("class", " discussion non-update"));
		else if (activity instanceof UpdatePullRequest)
			row.add(AttributeAppender.append("class", " non-discussion update"));
		else
			row.add(AttributeAppender.append("class", " non-discussion non-update"));
		
		PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
		final Date lastVisitDate = pullRequestManager.getLastVisitDate(getPullRequest());
		
		row.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String cssClasses = "";
				RenderableActivity activity = (RenderableActivity) row.getDefaultModelObject();
				if (activity instanceof CommentPullRequest) {
					CommentPullRequest commentActivity = (CommentPullRequest) activity;
					if (commentActivity.isCollapsed())
						cssClasses += " collapsed";
					if (commentActivity.getComment().isResolved())
						cssClasses += " resolved";
				} 
				if (lastVisitDate != null && lastVisitDate.before(activity.getDate()))
					cssClasses += " new";
				return cssClasses;
			}
			
		}));
		
		if (lastVisitDate != null && lastVisitDate.before(activity.getDate()))
			avatarColumn.add(AttributeAppender.append("title", "New activity since your last visit"));
		
		return row;
	}
	
	private List<RenderableActivity> getActivities() {
		PullRequest request = getPullRequest();
		List<RenderableActivity> renderableActivities = new ArrayList<>();

		renderableActivities.add(new OpenPullRequest(request));

		for (PullRequestUpdate update: request.getUpdates())
			renderableActivities.add(new UpdatePullRequest(update));
		
		for (PullRequestComment comment: request.getComments()) 
			renderableActivities.add(new CommentPullRequest(comment));
		
		for (PullRequestActivity activity: request.getActivities()) {
			if (activity.getAction() == PullRequestActivity.Action.INTEGRATE) {
				renderableActivities.add(new IntegratePullRequest(activity.getUser(), activity.getDate()));
			} else if (activity.getAction() == PullRequestActivity.Action.DISCARD) { 
				renderableActivities.add(new DiscardPullRequest(activity.getUser(), activity.getDate()));
			} else if (activity.getAction() == PullRequestActivity.Action.APPROVE) {
				renderableActivities.add(new ApprovePullRequest(activity.getRequest(), activity.getUser(), activity.getDate()));
			} else if (activity.getAction() == PullRequestActivity.Action.DISAPPROVE) {
				renderableActivities.add(new DisapprovePullRequest(activity.getRequest(), activity.getUser(), activity.getDate()));
			} else if (activity.getAction() == PullRequestActivity.Action.UNDO_REVIEW) {
				renderableActivities.add(new UndoReviewPullRequest(activity.getRequest(), activity.getUser(), activity.getDate()));
			} else if (activity.getAction() == PullRequestActivity.Action.REOPEN) {
				renderableActivities.add(new ReopenPullRequest(activity.getRequest(), activity.getUser(), activity.getDate()));
			} else {
				throw new IllegalStateException("Unexpected acvitity: " + activity.getAction());
			}
		}
		
		Collections.sort(renderableActivities, new Comparator<RenderableActivity>() {

			@Override
			public int compare(RenderableActivity o1, RenderableActivity o2) {
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
		
		return renderableActivities;
	}
	
	private Component newActivitiesView() {
		activitiesView = new RepeatingView("requestActivities") {
			
			@Override
			protected void onDetach() {
				/*
				 * Put the visit save logic here instead of page wide as the page.onDetach
				 * can be called sometimes before the page has not been rendered.   
				 */
				User user = getCurrentUser();
				if (user != null) {
					PullRequestVisit visit = getPullRequest().getVisit(user);
					if (visit == null) {
						visit = new PullRequestVisit();
						visit.setRequest(getPullRequest());
						visit.setUser(user);
						getPullRequest().getVisits().add(visit);
					} else {
						visit.setDate(new Date());
					}
					GitPlex.getInstance(Dao.class).persist(visit);
				}
				
				super.onDetach();
			}
			
		};
		activitiesView.setOutputMarkupId(true);
		
		List<RenderableActivity> activities = getActivities();
		
		for (RenderableActivity activity: activities) 
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
			List<RenderableActivity> activities = getActivities();
			Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
			RenderableActivity lastAcvitity = (RenderableActivity) lastActivityRow.getDefaultModelObject();
			for (RenderableActivity activity: activities) {
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
					GitPlex.getInstance(PullRequestCommentManager.class).save(comment, true);
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
		
		add(newBasicInfoContainer());
		add(newIntegrationStrategyContainer());
		add(newAssigneeContainer());
		add(newReviewersContainer());
		add(newWatchContainer());
	}

	private WebMarkupContainer newBasicInfoContainer() {
		PullRequest request = getPullRequest();
		WebMarkupContainer basicInfoContainer = new WebMarkupContainer("basicInfo");
		basicInfoContainer.add(new AvatarByUser("submitter", new UserModel(request.getSubmitter()), true));
		
		basicInfoContainer.add(new BranchLink("target", new EntityModel<Branch>(request.getTarget())));
		basicInfoContainer.add(new BranchLink("sourceLink", new AbstractReadOnlyModel<Branch>() {

			@Override
			public Branch getObject() {
				return getPullRequest().getSource();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getSource() != null);
			}
			
		});
		basicInfoContainer.add(new Label("sourceName", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequest request = getPullRequest();
				String branchName = Branch.getNameByFQN(request.getSourceFQN());
				String repoFQN = Branch.getRepositoryFQNByFQN(request.getSourceFQN());
				if (repoFQN.equals(request.getTarget().getRepository().getFQN())) 
					return branchName + " (removed)";
				else
					return request.getSourceFQN() + " (removed)";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getSource() == null);
			}
			
		});
		
		basicInfoContainer.add(new Link<Void>("restoreSource") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = requestModel.getObject();

				AuthorizationManager authorizationManager = GitPlex.getInstance(AuthorizationManager.class);
				if (request.getSource() != null || request.getSourceFQN() == null 
						|| !authorizationManager.canModifyRequest(request)) {
					setVisible(false);
				} else {
					String repositoryFQN = Branch.getRepositoryFQNByFQN(request.getSourceFQN());
					Repository repository = GitPlex.getInstance(RepositoryManager.class).findBy(repositoryFQN);
					if (repository == null) {
						setVisible(false);
					} else {
						String branchName = Branch.getNameByFQN(request.getSourceFQN());
						Branch branch = GitPlex.getInstance(BranchManager.class).findBy(repository, branchName);
						if (branch == null)
							setVisible(authorizationManager.canCreateBranch(repository, branchName));
						else
							setVisible(true);
					}
				}
			}

			@Override
			public void onClick() {
				GitPlex.getInstance(PullRequestManager.class).restoreSource(requestModel.getObject());
			}
			
		});
		
		return basicInfoContainer;
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
				
				ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());
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
				
				ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());
				setVisible(!SecurityUtils.getSubject().isPermitted(writePermission) || strategies.size() == 1);						
			}
			
		});

		ObjectPermission writePermission = ObjectPermission.ofRepositoryWrite(getRepository());

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
				setVisible(getCurrentUser() != null);
			}

		};
		
		watchContainer.setOutputMarkupId(true);

		final IModel<WatchStatus> optionModel = new LoadableDetachableModel<WatchStatus>() {

			@Override
			protected WatchStatus load() {
				PullRequestWatch watch = getPullRequest().getWatch(getCurrentUser());
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
				return String.valueOf(index);
			}
			
		};
		DropDownChoice<WatchStatus> choice = new DropDownChoice<>("option", optionModel, 
				options, choiceRenderer);
		
		choice.add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				PullRequestWatch watch = getPullRequest().getWatch(getCurrentUser());
				Dao dao = GitPlex.getInstance(Dao.class);
				if (optionModel.getObject() == WatchStatus.WATCHING) {
					if (watch != null) {
						watch.setIgnore(false);
						watch.setReason(null);
					} else {
						watch = new PullRequestWatch();
						watch.setRequest(getPullRequest());
						watch.setUser(getCurrentUser());
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
						watch.setUser(getCurrentUser());
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
				PullRequestWatch watch = getPullRequest().getWatch(getCurrentUser());
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
		final WebMarkupContainer reviewersContainer = new WebMarkupContainer("reviewers") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				if (request.getReviewInvitations().isEmpty()) {
					User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
					setVisible(request.isOpen() 
							&& !request.getPotentialReviewers().isEmpty()
							&& (request.getSubmitter().equals(currentUser) || SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(request.getTarget().getRepository()))));
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
				send(getPage(), Broadcast.BREADTH, new PullRequestChanged(target, getPullRequest()));								
			}
			
		});
		
		return reviewersContainer;
	}

}

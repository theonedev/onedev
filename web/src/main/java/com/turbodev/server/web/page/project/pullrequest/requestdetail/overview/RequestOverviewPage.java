package com.turbodev.server.web.page.project.pullrequest.requestdetail.overview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.PullRequestCommentManager;
import com.turbodev.server.manager.PullRequestManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.PullRequestComment;
import com.turbodev.server.model.PullRequestReference;
import com.turbodev.server.model.PullRequestStatusChange;
import com.turbodev.server.model.PullRequestUpdate;
import com.turbodev.server.model.PullRequestWatch;
import com.turbodev.server.model.User;
import com.turbodev.server.model.support.BranchProtection;
import com.turbodev.server.model.support.MergeStrategy;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.component.avatar.AvatarLink;
import com.turbodev.server.web.component.comment.CommentInput;
import com.turbodev.server.web.component.comment.ProjectAttachmentSupport;
import com.turbodev.server.web.component.markdown.AttachmentSupport;
import com.turbodev.server.web.component.requestreviewer.ReviewerListPanel;
import com.turbodev.server.web.component.verification.RequiredVerificationsPanel;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.RequestDetailPage;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity.CommentedActivity;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity.OpenedActivity;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity.ReferencedActivity;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity.RequestCommentDeleted;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity.StatusChangeActivity;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity.UpdatedActivity;
import com.turbodev.server.web.page.project.pullrequest.requestlist.RequestListPage;
import com.turbodev.server.web.util.ConfirmOnClick;
import com.turbodev.server.web.websocket.PageDataChanged;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class RequestOverviewPage extends RequestDetailPage {
	
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
	
	private Component newActivityRow(String id, PullRequestActivity activity) {
		WebMarkupContainer row = new WebMarkupContainer(id, Model.of(activity)) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof RequestCommentDeleted) {
					RequestCommentDeleted commentRemoved = (RequestCommentDeleted) event.getPayload();
					remove();
					commentRemoved.getHandler().appendJavaScript(String.format("$('#%s').remove();", getMarkupId()));
				} 
			}
			
		};
		row.setOutputMarkupId(true);
		String anchor = activity.getAnchor();
		if (anchor != null)
			row.setMarkupId(anchor);
		
		if (row.get("content") == null) 
			row.add(activity.render("content"));
		
		WebMarkupContainer avatarColumn = new WebMarkupContainer("avatar");
		row.add(avatarColumn);
		
		if (activity instanceof OpenedActivity) {
			row.add(AttributeAppender.append("class", " discussion"));
			PullRequest request = ((OpenedActivity)activity).getRequest();
			avatarColumn.add(new AvatarLink("avatar", 
					User.getForDisplay(request.getSubmitter(), request.getSubmitterName())));
		} else if (activity instanceof CommentedActivity) {
			row.add(AttributeAppender.append("class", " discussion"));
			PullRequestComment comment = ((CommentedActivity)activity).getComment();
			avatarColumn.add(new AvatarLink("avatar", User.getForDisplay(comment.getUser(), comment.getUserName())));
		} else {
			row.add(AttributeAppender.append("class", " non-discussion"));
			avatarColumn.add(new WebMarkupContainer("avatar"));
		}
		
		if (activity instanceof UpdatedActivity)
			row.add(AttributeAppender.append("class", " update"));
		else
			row.add(AttributeAppender.append("class", " non-update"));

		return row;
	}
	
	private List<PullRequestActivity> getActivities() {
		PullRequest request = getPullRequest();
		List<PullRequestActivity> activities = new ArrayList<>();

		activities.add(new OpenedActivity(request));
		
		for (PullRequestUpdate update: request.getUpdates())
			activities.add(new UpdatedActivity(update));
		
		for (PullRequestComment comment: request.getComments()) { 
			activities.add(new CommentedActivity(comment));
		}
		
		for (PullRequestReference reference: request.getReferencedBy()) {
			activities.add(new ReferencedActivity(reference));
		}
		
		for (PullRequestStatusChange statusChange: request.getStatusChanges()) {
			activities.add(new StatusChangeActivity(statusChange));
		}
		
		activities.sort((o1, o2) -> {
			if (o1.getDate().getTime()<o2.getDate().getTime())
				return -1;
			else if (o1.getDate().getTime()>o2.getDate().getTime())
				return 1;
			else if (o1 instanceof OpenedActivity)
				return -1;
			else
				return 1;
		});
		
		return activities;
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PageDataChanged) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			IPartialPageRequestHandler partialPageRequestHandler = pageDataChanged.getHandler();

			@SuppressWarnings("deprecation")
			Component prevActivityRow = activitiesView.get(activitiesView.size()-1);
			PullRequestActivity lastActivity = (PullRequestActivity) prevActivityRow.getDefaultModelObject();
			List<PullRequestActivity> newActivities = new ArrayList<>();
			for (PullRequestActivity activity: getActivities()) {
				if (activity.getDate().getTime() > lastActivity.getDate().getTime())
					newActivities.add(activity);
			}

			Component sinceChangesRow = null;
			for (Component row: activitiesView) {
				if (row.getDefaultModelObject() == null) {
					sinceChangesRow = row;
					break;
				}
			}

			if (sinceChangesRow == null && !newActivities.isEmpty()) {
				Date sinceDate = new DateTime(newActivities.iterator().next().getDate()).minusMillis(1).toDate();
				sinceChangesRow = newSinceChangesRow(activitiesView.newChildId(), sinceDate);
				activitiesView.add(sinceChangesRow);
				String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
						sinceChangesRow.getMarkupId(), prevActivityRow.getMarkupId());
				partialPageRequestHandler.prependJavaScript(script);
				partialPageRequestHandler.add(sinceChangesRow);
				prevActivityRow = sinceChangesRow;
			}
			
			for (PullRequestActivity activity: newActivities) {
				Component newActivityRow = newActivityRow(activitiesView.newChildId(), activity); 
				newActivityRow.add(AttributeAppender.append("class", "new"));
				activitiesView.add(newActivityRow);
				
				String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
						newActivityRow.getMarkupId(), prevActivityRow.getMarkupId());
				partialPageRequestHandler.prependJavaScript(script);
				partialPageRequestHandler.add(newActivityRow);
				
				if (activity instanceof UpdatedActivity) {
					partialPageRequestHandler.appendJavaScript("$('tr.since-changes').addClass('visible');");
				}
				prevActivityRow = newActivityRow;
			}
		}
	}

	private Component newSinceChangesRow(String id, Date sinceDate) {
		WebMarkupContainer row = new WebMarkupContainer(id);
		row.setOutputMarkupId(true);
		row.add(AttributeAppender.append("class", " non-discussion"));
		
		WebMarkupContainer avatarColumn = new WebMarkupContainer("avatar");
		avatarColumn.add(new WebMarkupContainer("avatar"));
		row.add(avatarColumn);
		
		WebMarkupContainer contentColumn = new Fragment("content", "sinceChangesRowContentFrag", this);
		contentColumn.add(new SinceChangesLink("sinceChanges", requestModel, sinceDate));
		row.add(contentColumn);
		
		row.add(AttributeAppender.append("class", "since-changes"));
		
		return row;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(activitiesView = new RepeatingView("requestActivities"));
		activitiesView.setOutputMarkupId(true);
		
		List<PullRequestActivity> activities = getActivities();

		PullRequest request = getPullRequest();
		List<PullRequestActivity> oldActivities = new ArrayList<>();
		List<PullRequestActivity> newActivities = new ArrayList<>();
		for (PullRequestActivity activity: activities) {
			if (request.isVisitedAfter(activity.getDate())) {
				oldActivities.add(activity);
			} else {
				newActivities.add(activity);
			}
		}

		for (PullRequestActivity activity: oldActivities) {
			activitiesView.add(newActivityRow(activitiesView.newChildId(), activity));
		}

		if (!oldActivities.isEmpty() && !newActivities.isEmpty()) {
			Date sinceDate = new DateTime(newActivities.iterator().next().getDate()).minusMillis(1).toDate();
			Component row = newSinceChangesRow(activitiesView.newChildId(), sinceDate);
			for (PullRequestActivity activity: newActivities) {
				if (activity instanceof UpdatedActivity) {
					row.add(AttributeAppender.append("class", "visible"));
					break;
				}
			}
			activitiesView.add(row);
		}
		
		for (PullRequestActivity activity: newActivities) {
			Component row = newActivityRow(activitiesView.newChildId(), activity);
			row.add(AttributeAppender.append("class", "new"));
			activitiesView.add(row);
		}
		
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
		
		String autosaveKey = "autosave:addPullRequestComment:" + getPullRequest().getId();
		
		CommentInput input = new CommentInput("input", Model.of(""), false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getPullRequest().getUUID());
			}

			@Override
			protected Project getProject() {
				return RequestOverviewPage.this.getProject();
			}
			
			@Override
			protected String getAutosaveKey() {
				return autosaveKey;
			}
			
			@Override
			protected List<AttributeModifier> getInputModifiers() {
				return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
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
				TurboDev.getInstance(PullRequestCommentManager.class).save(comment);
				input.setModelObject("");

				target.add(addComment);
				
				@SuppressWarnings("deprecation")
				Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
				Component newActivityRow = newActivityRow(activitiesView.newChildId(), new CommentedActivity(comment)); 
				activitiesView.add(newActivityRow);
				
				String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
						newActivityRow.getMarkupId(), lastActivityRow.getMarkupId());
				target.prependJavaScript(script);
				target.add(newActivityRow);
				target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
		
		WebMarkupContainer sideInfoContainer = new WebMarkupContainer("sideInfo") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					IPartialPageRequestHandler partialPageRequestHandler = pageDataChanged.getHandler();
					partialPageRequestHandler.add(this);
				}
				
			}
			
		};
		sideInfoContainer.setOutputMarkupId(true);
		add(sideInfoContainer);
		
		sideInfoContainer.add(newMergeStrategyContainer());
		sideInfoContainer.add(new ReviewerListPanel("reviewers", requestModel));
		
		BranchProtection protection = request.getTargetProject().getBranchProtection(request.getTargetBranch());
		if (protection != null && !protection.getVerifications().isEmpty() && protection.isVerifyMerges()) {
			sideInfoContainer.add(new Label("verifyMerges", "(On Merged Commit)"));
		} else {
			sideInfoContainer.add(new WebMarkupContainer("verifyMerges"));
		}
		sideInfoContainer.add(new RequiredVerificationsPanel("verifications", requestModel));
		
		sideInfoContainer.add(newWatchContainer());
		sideInfoContainer.add(newManageContainer());
	}
	
	private WebMarkupContainer newManageContainer() {
		WebMarkupContainer container = new WebMarkupContainer("manage");
		container.setVisible(SecurityUtils.canModify(getPullRequest()));
		container.add(new Link<Void>("synchronize") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen());
			}

			@Override
			public void onClick() {
				TurboDev.getInstance(PullRequestManager.class).check(getPullRequest());
				Session.get().success("Pull request is synchronized");
			}
			
		});
		container.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				PullRequest request = getPullRequest();
				TurboDev.getInstance(PullRequestManager.class).delete(request);
				Session.get().success("Pull request #" + request.getNumber() + " is deleted");
				setResponsePage(RequestListPage.class, RequestListPage.paramsOf(getProject()));
			}
			
		}.add(new ConfirmOnClick("Do you really want to delete this pull request?")));
		return container;
	}

	private WebMarkupContainer newMergeStrategyContainer() {
		WebMarkupContainer mergeStrategyContainer = new WebMarkupContainer("mergeStrategy");
		mergeStrategyContainer.setOutputMarkupId(true);

		IModel<MergeStrategy> mergeStrategyModel = new IModel<MergeStrategy>() {

			@Override
			public void detach() {
			}

			@Override
			public MergeStrategy getObject() {
				return getPullRequest().getMergeStrategy();
			}

			@Override
			public void setObject(MergeStrategy object) {
				getPullRequest().setMergeStrategy(object);
			}
			
		};
		
		List<MergeStrategy> mergeStrategies = Arrays.asList(MergeStrategy.values());
		DropDownChoice<MergeStrategy> editor = 
				new DropDownChoice<MergeStrategy>("editor", mergeStrategyModel, mergeStrategies) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getPullRequest().isMerged() && SecurityUtils.canModify(getPullRequest()));						
			}
			
		};
		editor.add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				TurboDev.getInstance(PullRequestManager.class).saveMergeStrategy(getPullRequest());
				send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
			}
			
		});
		mergeStrategyContainer.add(editor);
		
		mergeStrategyContainer.add(new Label("viewer", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().getDisplayName();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().isMerged() || !SecurityUtils.canModify(getPullRequest()));						
			}
			
		});

		mergeStrategyContainer.add(new Label("help", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().getDescription();
			}
			
		}) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getPullRequest().isMerged() && SecurityUtils.canModify(getPullRequest()));						
			}
			
		});
		
		return mergeStrategyContainer;
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
				Dao dao = TurboDev.getInstance(Dao.class);
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

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RequestOverviewResourceReference()));
	}
	
}

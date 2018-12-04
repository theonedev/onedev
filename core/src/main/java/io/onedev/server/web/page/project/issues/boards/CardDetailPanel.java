package io.onedev.server.web.page.project.issues.boards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.FixedIssueCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.component.issue.activities.IssueActivitiesPanel;
import io.onedev.server.web.component.issue.operation.IssueOperationsPanel;
import io.onedev.server.web.component.issue.pullrequests.IssuePullRequestsPanel;
import io.onedev.server.web.component.issue.side.IssueSidePanel;
import io.onedev.server.web.component.issue.title.IssueTitlePanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPositionSupport;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;

@SuppressWarnings("serial")
abstract class CardDetailPanel extends GenericPanel<Issue> implements InputContext {

	private static final String TAB_CONTENT_ID = "tabContent";
	
	@SuppressWarnings("unused")
	private String buildQuery;
	
	private IssueActivitiesPanel activities;
	
	public CardDetailPanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
	private Project getProject() {
		return getIssue().getProject();
	}
	
	private IssueActivitiesPanel newActivitiesPanel() {
		IssueActivitiesPanel activities = new IssueActivitiesPanel(TAB_CONTENT_ID) {

			@Override
			protected Issue getIssue() {
				return CardDetailPanel.this.getIssue();
			}
			
		};
		activities.setOutputMarkupId(true);
		return activities;
	}
	
	@Override
	protected void onBeforeRender() {
		addOrReplace(new IssueTitlePanel("title") {

			@Override
			protected Issue getIssue() {
				return CardDetailPanel.this.getIssue();
			}

		});
		
		addOrReplace(new IssueOperationsPanel("operations") {

			@Override
			protected Issue getIssue() {
				return CardDetailPanel.this.getIssue();
			}

			@Override
			protected void onStateChanged(AjaxRequestTarget target) {
				target.add(CardDetailPanel.this);
			}

			@Override
			protected Component newCreateIssueButton(String componentId, String templateQuery) {
				return new WebMarkupContainer(componentId).setVisible(false);
			}
			
		});

		List<Tab> tabs = new ArrayList<>();
		tabs.add(new AjaxActionTab(Model.of("Activities")) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				activities = newActivitiesPanel();
				CardDetailPanel.this.replace(activities);
				target.add(activities);
			}

			@Override
			protected Component renderOptions(String componentId) {
				return activities.renderOptions(componentId);
			}
			
		});
		
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class); 
		Collection<ObjectId> fixCommits = commitInfoManager.getFixCommits(getProject(), getIssue().getNumber());
		
		if (SecurityUtils.canReadCode(getProject().getFacade())) {
			if (!fixCommits.isEmpty()) {		
				tabs.add(new AjaxActionTab(Model.of("Commits")) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						Component content = new CommitListPanel(TAB_CONTENT_ID, new AbstractReadOnlyModel<Project>() {

							@Override
							public Project getObject() {
								return getIssue().getProject();
							}
							
						}, new AbstractReadOnlyModel<List<RevCommit>>() {

							@Override
							public List<RevCommit> getObject() {
								return getIssue().getCommits();
							}
							
						}).setOutputMarkupId(true);
						CardDetailPanel.this.replace(content);
						target.add(content);
					}
					
				});
			}
			CodeCommentRelationInfoManager codeCommentRelationInfoManager = OneDev.getInstance(CodeCommentRelationInfoManager.class); 
			Collection<Long> pullRequestIds = new HashSet<>();
			for (ObjectId commit: fixCommits) 
				pullRequestIds.addAll(codeCommentRelationInfoManager.getPullRequestIds(getProject(), commit));		
			if (!pullRequestIds.isEmpty()) {
				tabs.add(new AjaxActionTab(Model.of("Pull Requests")) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						Component content = new IssuePullRequestsPanel(TAB_CONTENT_ID) {

							@Override
							protected Issue getIssue() {
								return CardDetailPanel.this.getIssue();
							}
							
						}.setOutputMarkupId(true);
						CardDetailPanel.this.replace(content);
						target.add(content);
					}
					
				});
			}
		}
		if (!fixCommits.isEmpty()) {
			tabs.add(new AjaxActionTab(Model.of("Builds")) {

				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component content = new BuildListPanel(TAB_CONTENT_ID, new PropertyModel<String>(CardDetailPanel.this, "buildQuery")) {

						@Override
						protected Project getProject() {
							return getIssue().getProject();
						}

						@Override
						protected BuildQuery getBaseQuery() {
							return new BuildQuery(new FixedIssueCriteria(getIssue()), new ArrayList<>());
						}

						@Override
						protected PagingHistorySupport getPagingHistorySupport() {
							return null;
						}

						@Override
						protected void onQueryUpdated(AjaxRequestTarget target) {
						}

						@Override
						protected QuerySaveSupport getQuerySaveSupport() {
							return null;
						}

					}.setOutputMarkupId(true);
					
					CardDetailPanel.this.replace(content);
					target.add(content);
				}
				
			});
		}
		
		addOrReplace(new Tabbable("tabs", tabs).setOutputMarkupId(true));
		
		addOrReplace(new IssueSidePanel("side") {

			@Override
			protected Issue getIssue() {
				return CardDetailPanel.this.getIssue();
			}

			@Override
			protected QueryPositionSupport<Issue> getQueryPositionSupport() {
				return CardDetailPanel.this.getQueryPositionSupport();
			}

			@Override
			protected Component newDeleteLink(String componentId) {
				AjaxLink<Void> deleteLink = new AjaxLink<Void>(componentId) {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this issue?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						OneDev.getInstance(IssueManager.class).delete(SecurityUtils.getUser(), getIssue());
						onDeletedIssue(target);
					}
					
				};
				deleteLink.setVisible(SecurityUtils.canAdministrate(getIssue().getProject().getFacade()));
				return deleteLink;
			}
			
		});
		addOrReplace(activities = newActivitiesPanel());
		
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RequestCycle.get().getListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getUser() != null) 
					OneDev.getInstance(UserInfoManager.class).visitIssue(SecurityUtils.getUser(), getIssue());
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});	
		
		add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	public List<String> getInputNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public InputSpec getInputSpec(String inputName) {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpec(inputName);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		String script = String.format("onedev.server.issueBoards.onCardDetailLoad('%s')", getMarkupId());
		// Use onload to make sure perfect scrollbar working 
		response.render(OnLoadHeaderItem.forScript(script));
	}

	protected abstract void onClose(AjaxRequestTarget target);
	
	@Nullable
	protected abstract QueryPositionSupport<Issue> getQueryPositionSupport();
	
	protected abstract void onDeletedIssue(AjaxRequestTarget target);
}

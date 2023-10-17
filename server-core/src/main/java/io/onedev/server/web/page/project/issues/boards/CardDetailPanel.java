package io.onedev.server.web.page.project.issues.boards;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.xodus.VisitInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.FixedIssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.issue.activities.IssueActivitiesPanel;
import io.onedev.server.web.component.issue.authorizations.IssueAuthorizationsPanel;
import io.onedev.server.web.component.issue.commits.IssueCommitsPanel;
import io.onedev.server.web.component.issue.editabletitle.IssueEditableTitlePanel;
import io.onedev.server.web.component.issue.operation.IssueOperationsPanel;
import io.onedev.server.web.component.issue.pullrequests.IssuePullRequestsPanel;
import io.onedev.server.web.component.issue.side.IssueSidePanel;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.util.CursorSupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
abstract class CardDetailPanel extends GenericPanel<Issue> implements InputContext {

	private static final String TAB_CONTENT_ID = "tabContent";
	
	private IssueActivitiesPanel activities;
	
	public CardDetailPanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	private Issue getIssue() {
		return getModelObject();
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
	protected void onInitialize() {
		super.onInitialize();

		add(new IssueEditableTitlePanel("title") {

			@Override
			protected Issue getIssue() {
				return CardDetailPanel.this.getIssue();
			}

			@Override
			protected Project getProject() {
				return CardDetailPanel.this.getProject();
			}

		});
		
		add(new SideInfoLink("moreInfoTrigger"));
		
		add(new IssueOperationsPanel("operations") {

			@Override
			protected Issue getIssue() {
				return CardDetailPanel.this.getIssue();
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
		
		if (!getIssue().getCommits().isEmpty()) {
			if (SecurityUtils.canReadCode(getIssue().getProject())) {
				tabs.add(new AjaxActionTab(Model.of("Fixing Commits")) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						Component content = new IssueCommitsPanel(TAB_CONTENT_ID, CardDetailPanel.this.getModel());
						content.setOutputMarkupId(true);
						CardDetailPanel.this.replace(content);
						target.add(content);
					}
					
				});
				if (!getIssue().getPullRequests().isEmpty()) {
					tabs.add(new AjaxActionTab(Model.of("Pull Requests")) {

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							Component content = new IssuePullRequestsPanel(TAB_CONTENT_ID, new AbstractReadOnlyModel<Issue>() {

								@Override
								public Issue getObject() {
									return getIssue();
								}
								
							});
							CardDetailPanel.this.replace(content);
							target.add(content);
						}
						
					});
				}
			}
			
			tabs.add(new AjaxActionTab(Model.of("Fixing Builds")) {

				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component content = new BuildListPanel(TAB_CONTENT_ID, Model.of((String)null), true, true, 0) {

						@Override
						protected BuildQuery getBaseQuery() {
							return new BuildQuery(new FixedIssueCriteria(getIssue()), new ArrayList<>());
						}

						@Override
						protected Project getProject() {
							return getIssue().getProject();
						}

					}.setOutputMarkupId(true);
					
					CardDetailPanel.this.replace(content);
					target.add(content);
				}
				
			});
			
			if (getIssue().isConfidential() && SecurityUtils.canModify(getIssue())) {
				tabs.add(new AjaxActionTab(Model.of("Authorizations")) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						Component content = new IssueAuthorizationsPanel(TAB_CONTENT_ID) {

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
		
		add(new Tabbable("tabs", tabs).setOutputMarkupId(true));
		
		add(new SideInfoPanel("moreInfo") {

			@Override
			protected Component newBody(String componentId) {
				return new IssueSidePanel(componentId) {
					
					@Override
					protected Issue getIssue() {
						return CardDetailPanel.this.getIssue();
					}

					@Override
					protected Component newDeleteLink(String componentId) {
						return new AjaxLink<Void>(componentId) {

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this issue?"));
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								OneDev.getInstance(IssueManager.class).delete(getIssue());
								onDeletedIssue(target);
							}
							
						};
					}

				};
			}

			@Override
			protected Component newTitle(String componentId) {
				return new EntityNavPanel<Issue>(componentId) {

					@Override
					protected EntityQuery<Issue> parse(String queryString, Project project) {
						IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
						return IssueQuery.parse(project, queryString, option, true);
					}

					@Override
					protected Issue getEntity() {
						return getIssue();
					}

					@Override
					protected List<Issue> query(EntityQuery<Issue> query, int offset, int count, ProjectScope projectScope) {
						IssueManager issueManager = OneDev.getInstance(IssueManager.class);
						return issueManager.query(projectScope, query, false, offset, count);
					}

					@Override
					protected CursorSupport<Issue> getCursorSupport() {
						return CardDetailPanel.this.getCursorSupport();
					}
					
				};
				
			}
			
		});
		
		add(activities = newActivitiesPanel());
		
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
					OneDev.getInstance(VisitInfoManager.class).visitIssue(SecurityUtils.getUser(), getIssue());
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});	
		
		setOutputMarkupId(true);
	}

	@Override
	public List<String> getInputNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpec(inputName);
	}

	protected abstract Project getProject();
	
	protected abstract void onClose(AjaxRequestTarget target);
	
	@Nullable
	protected abstract CursorSupport<Issue> getCursorSupport();
	
	protected abstract void onDeletedIssue(AjaxRequestTarget target);
}

package io.onedev.server.web.page.project.issues.boards;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.SettingService;
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
import io.onedev.server.web.component.issue.primary.IssuePrimaryPanel;
import io.onedev.server.web.component.issue.pullrequests.IssuePullRequestsPanel;
import io.onedev.server.web.component.issue.side.IssueSidePanel;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.util.CursorSupport;
import io.onedev.server.xodus.VisitInfoService;

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
		
		add(new IssuePrimaryPanel("primary") {

			@Override
			protected Issue getIssue() {
				return CardDetailPanel.this.getIssue();
			}

		});
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new AjaxActionTab(Model.of(_T("Activities"))) {

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
		
		if (!getIssue().getFixCommits(false).isEmpty()) {
			if (SecurityUtils.canReadCode(getIssue().getProject())) {
				tabs.add(new AjaxActionTab(Model.of(_T("Fixing Commits"))) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						Component content = new IssueCommitsPanel(TAB_CONTENT_ID, CardDetailPanel.this.getModel());
						content.setOutputMarkupId(true);
						CardDetailPanel.this.replace(content);
						target.add(content);
					}
					
				});
				if (!getIssue().getPullRequests().isEmpty()) {
					tabs.add(new AjaxActionTab(Model.of(_T("Pull Requests"))) {

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
			
			tabs.add(new AjaxActionTab(Model.of(_T("Fixing Builds"))) {

				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component content = new BuildListPanel(TAB_CONTENT_ID, Model.of((String)null), true, true) {

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
			
			if (getIssue().isConfidential() && SecurityUtils.canModifyIssue(getIssue())) {
				tabs.add(new AjaxActionTab(Model.of(_T("Authorizations"))) {

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
								attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this issue?")));
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								OneDev.getInstance(IssueService.class).delete(getIssue());
								var oldAuditContent = VersionedXmlDoc.fromBean(getIssue()).toXML();
								OneDev.getInstance(AuditService.class).audit(getIssue().getProject(), "deleted issue \"" + getIssue().getReference().toString(getIssue().getProject()) + "\"", oldAuditContent, null);
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
						IssueService issueService = OneDev.getInstance(IssueService.class);
						return issueService.query(SecurityUtils.getSubject(), projectScope, query, false, offset, count);
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
		
		RequestCycle.get().getListeners().add(new AbstractRequestCycleListener() {
						
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getAuthUser() != null) 
					OneDev.getInstance(VisitInfoService.class).visitIssue(SecurityUtils.getAuthUser(), getIssue());
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
		return OneDev.getInstance(SettingService.class).getIssueSetting().getFieldSpec(inputName);
	}

	protected abstract Project getProject();
	
	protected abstract void onClose(AjaxRequestTarget target);
	
	@Nullable
	protected abstract CursorSupport<Issue> getCursorSupport();
	
	protected abstract void onDeletedIssue(AjaxRequestTarget target);
}

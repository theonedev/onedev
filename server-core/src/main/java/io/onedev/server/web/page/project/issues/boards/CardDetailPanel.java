package io.onedev.server.web.page.project.issues.boards;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.issue.editabletitle.IssueEditableTitlePanel;
import io.onedev.server.web.component.issue.operation.IssueOperationsPanel;
import io.onedev.server.web.component.issue.primary.IssuePrimaryPanel;
import io.onedev.server.web.component.issue.side.IssueSidePanel;
import io.onedev.server.web.component.issue.tabs.IssueTabsPanel;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.util.CursorSupport;
import io.onedev.server.xodus.VisitInfoManager;

abstract class CardDetailPanel extends GenericPanel<Issue> implements InputContext {
		
	public CardDetailPanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	private Issue getIssue() {
		return getModelObject();
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
		
		add(new IssueTabsPanel("tabs", getModel()));
		
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
						IssueQueryParseOption option = new IssueQueryParseOption()
							.withCurrentProjectCriteria(true)
							.withCurrentUserCriteria(true);
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
					OneDev.getInstance(VisitInfoManager.class).visitIssue(SecurityUtils.getAuthUser(), getIssue());
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

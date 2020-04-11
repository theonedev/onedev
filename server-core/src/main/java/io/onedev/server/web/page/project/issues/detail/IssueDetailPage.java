package io.onedev.server.web.page.project.issues.detail;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.util.script.identity.SiteAdministrator;
import io.onedev.server.web.component.issue.operation.IssueOperationsPanel;
import io.onedev.server.web.component.issue.side.IssueSidePanel;
import io.onedev.server.web.component.issue.title.IssueTitlePanel;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;

@SuppressWarnings("serial")
public abstract class IssueDetailPage extends ProjectPage implements InputContext, ScriptIdentityAware {

	public static final String PARAM_ISSUE = "issue";
	
	protected final IModel<Issue> issueModel;
	
	private final Cursor cursor;
	
	public IssueDetailPage(PageParameters params) {
		super(params);
		
		String issueNumberString = params.get(PARAM_ISSUE).toString();
		if (StringUtils.isBlank(issueNumberString))
			throw new RestartResponseException(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(getProject(), null, 0));
		
		issueModel = new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				Long issueNumber = Long.valueOf(issueNumberString);
				Issue issue = OneDev.getInstance(IssueManager.class).find(getProject(), issueNumber);
				if (issue == null)
					throw new EntityNotFoundException("Unable to find issue #" + issueNumber + " in project " + getProject());
				else if (!issue.getProject().equals(getProject()))
					throw new RestartResponseException(getPageClass(), paramsOf(issue, cursor));
				else
					return issue;
			}

		};
	
		cursor = Cursor.from(params);
	}
	
	public Issue getIssue() {
		return issueModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WorkflowChangeAlertPanel("workflowChangeAlert") {

			@Override
			protected void onCompleted(AjaxRequestTarget target) {
				setResponsePage(getPageClass(), getPageParameters());
			}
			
		});
		add(new IssueTitlePanel("title", true) {

			@Override
			protected Issue getIssue() {
				return IssueDetailPage.this.getIssue();
			}

		});
		
		add(new IssueOperationsPanel("operations") {

			@Override
			protected Issue getIssue() {
				return IssueDetailPage.this.getIssue();
			}

		});

		List<Tab> tabs = new ArrayList<>();
		tabs.add(new IssueTab("Activities", IssueActivitiesPage.class) {

			@Override
			protected Component renderOptions(String componentId) {
				IssueActivitiesPage page = (IssueActivitiesPage) getPage();
				return page.renderOptions(componentId);
			}
			
		});
		
		if (!getIssue().getCommits().isEmpty()) {
			if (SecurityUtils.canReadCode(getProject())) {
				tabs.add(new IssueTab("Fixing Commits", IssueCommitsPage.class));
				if (!getIssue().getPullRequests().isEmpty())
					tabs.add(new IssueTab("Pull Requests", IssuePullRequestsPage.class));
			}
			// Do not calculate fix builds now as it might be slow
			tabs.add(new IssueTab("Fixing Builds", IssueBuildsPage.class));
		}
		
		add(new Tabbable("issueTabs", tabs).setOutputMarkupId(true));
		
		add(new SideInfoPanel("moreInfo") {

			@Override
			protected Component newContent(String componentId) {
				return new IssueSidePanel(componentId) {

					@Override
					protected Issue getIssue() {
						return IssueDetailPage.this.getIssue();
					}

					@Override
					protected CursorSupport<Issue> getCursorSupport() {
						return new CursorSupport<Issue>() {

							@Override
							public Cursor getCursor() {
								return cursor;
							}

							@Override
							public void navTo(AjaxRequestTarget target, Issue entity, Cursor cursor) {
								PageParameters params = IssueDetailPage.paramsOf(entity, cursor);
								setResponsePage(getPageClass(), params);
							}
							
						};
					}

					@Override
					protected Component newDeleteLink(String componentId) {
						Link<Void> deleteLink = new Link<Void>(componentId) {

							@Override
							public void onClick() {
								OneDev.getInstance(IssueManager.class).delete(getIssue());
								PageParameters params = ProjectIssueListPage.paramsOf(
										getProject(), 
										Cursor.getQuery(cursor), 
										Cursor.getPage(cursor) + 1); 
								setResponsePage(ProjectIssueListPage.class, params);
							}
							
						};
						deleteLink.add(new ConfirmOnClick("Do you really want to delete this issue?"));
						deleteLink.setVisible(SecurityUtils.canManageIssues(getIssue().getProject()));
						return deleteLink;
					}
					
				};
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
					OneDev.getInstance(UserInfoManager.class).visitIssue(SecurityUtils.getUser(), getIssue());
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});	

	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueDetailResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.issueDetail.onDomReady();"));
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Issue issue, @Nullable Cursor cursor) {
		return paramsOf(issue.getFQN(), cursor);
	}

	public static PageParameters paramsOf(ProjectScopedNumber issueFQN, @Nullable Cursor cursor) {
		PageParameters params = ProjectPage.paramsOf(issueFQN.getProject());
		params.add(PARAM_ISSUE, issueFQN.getNumber());
		if (cursor != null)
			cursor.fill(params);
		return params;
	}
	
	@Override
	public List<String> getInputNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldSpec getInputSpec(String inputName) {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpec(inputName);
	}
	
	@Override
	public ScriptIdentity getScriptIdentity() {
		return new SiteAdministrator();
	}

	private class IssueTab extends PageTab {

		public IssueTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabLink(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getIssue(), cursor));
				}
				
			};
		}
		
	}
	
}

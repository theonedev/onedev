package io.onedev.server.web.page.project.issues.detail;

import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.xodus.VisitInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.issue.editabletitle.IssueEditableTitlePanel;
import io.onedev.server.web.component.issue.operation.IssueOperationsPanel;
import io.onedev.server.web.component.issue.side.IssueSidePanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("serial")
public abstract class IssueDetailPage extends ProjectIssuesPage implements InputContext {

	public static final String PARAM_ISSUE = "issue";
	
	protected final IModel<Issue> issueModel;
	
	public IssueDetailPage(PageParameters params) {
		super(params);
		
		String issueNumberString = params.get(PARAM_ISSUE).toString();
		if (StringUtils.isBlank(issueNumberString)) {
			throw new RestartResponseException(ProjectIssueListPage.class, 
					ProjectIssueListPage.paramsOf(getProject(), null, 0));
		}
		
		issueModel = new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				Long issueNumber;
				try {
					issueNumber = Long.valueOf(issueNumberString);
				} catch (NumberFormatException e) {
					throw new ValidationException("Invalid issue number: " + issueNumberString);
				}
				
				Issue issue = getIssueManager().find(getProject(), issueNumber);
				if (issue == null) { 
					throw new EntityNotFoundException("Unable to find issue #" + issueNumber + " in project " + getProject());
				} else {
					OneDev.getInstance(IssueLinkManager.class).loadDeepLinks(issue);
					if (!issue.getProject().equals(getProject())) 
						throw new RestartResponseException(getPageClass(), paramsOf(issue));
					return issue;
				}
			}

		};
	}
	
	public Issue getIssue() {
		return issueModel.getObject();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccess(getIssue());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new IssueEditableTitlePanel("title") {

			@Override
			protected Issue getIssue() {
				return IssueDetailPage.this.getIssue();
			}

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

		});
		
		add(new SideInfoLink("moreInfo"));
		
		add(new IssueOperationsPanel("operations") {

			@Override
			protected Issue getIssue() {
				return IssueDetailPage.this.getIssue();
			}

		});
		
		add(new Tabbable("issueTabs", new LoadableDetachableModel<>() {

			@Override
			protected List<? extends Tab> load() {
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

				if (getIssue().isConfidential() && SecurityUtils.canModify(getIssue()))
					tabs.add(new IssueTab("Authorizations", IssueAuthorizationsPage.class));

				return tabs;
			}

		}) {
			@Override
			public void onInitialize() {
				super.onInitialize();
				
				add(new ChangeObserver() {

					@Override
					public Collection<String> findObservables() {
						return Lists.newArrayList(Issue.getDetailChangeObservable(getIssue().getId()));
					}

				});
				
				setOutputMarkupId(true);
			}
		});
		
		add(new SideInfoPanel("side") {

			@Override
			protected Component newBody(String componentId) {
				return new IssueSidePanel(componentId) {

					@Override
					protected Issue getIssue() {
						return IssueDetailPage.this.getIssue();
					}

					@Override
					protected Component newDeleteLink(String componentId) {
						return new Link<Void>(componentId) {

							@Override
							public void onClick() {
								getIssueManager().delete(getIssue());
								Session.get().success("Issue #" + getIssue().getNumber() + " deleted");
								
								String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Issue.class);
								if (redirectUrlAfterDelete != null)
									throw new RedirectToUrlException(redirectUrlAfterDelete);
								else
									setResponsePage(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(getProject()));
							}
							
						}.add(new ConfirmClickModifier("Do you really want to delete this issue?"));
					}

				};
			}

			@Override
			protected Component newTitle(String componentId) {
				return new EntityNavPanel<Issue>(componentId) {

					@Override
					protected EntityQuery<Issue> parse(String queryString, Project project) {
						IssueQueryParseOption option = new IssueQueryParseOption()
								.withCurrentUserCriteria(true)
								.withCurrentProjectCriteria(true);
						return IssueQuery.parse(project, queryString, option, true);
					}

					@Override
					protected Issue getEntity() {
						return getIssue();
					}

					@Override
					protected List<Issue> query(EntityQuery<Issue> query, int offset, int count, ProjectScope projectScope) {
						return getIssueManager().query(projectScope, query, false, offset, count);
					}

					@Override
					protected CursorSupport<Issue> getCursorSupport() {
						return new CursorSupport<Issue>() {

							@Override
							public Cursor getCursor() {
								return WebSession.get().getIssueCursor();
							}

							@Override
							public void navTo(AjaxRequestTarget target, Issue entity, Cursor cursor) {
								WebSession.get().setIssueCursor(cursor);
								setResponsePage(getPageClass(), paramsOf(entity));
							}
							
						};
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
					OneDev.getInstance(VisitInfoManager.class).visitIssue(SecurityUtils.getUser(), getIssue());
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});	

	}
	
	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Issue issue) {
		return paramsOf(issue.getFQN());
	}

	public static PageParameters paramsOf(ProjectScopedNumber issueFQN) {
		PageParameters params = ProjectPage.paramsOf(issueFQN.getProject());
		params.add(PARAM_ISSUE, issueFQN.getNumber());
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

	private class IssueTab extends PageTab {

		public IssueTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getIssue()));
				}
				
			};
		}
		
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("issues", ProjectIssueListPage.class, 
				ProjectIssueListPage.paramsOf(getProject(), 0)));
		fragment.add(new Label("issueNumber", "#" + getIssue().getNumber()));
		return fragment;
	}

	@Override
	protected String getPageTitle() {
		return getIssue().getTitle() + " - Issue #" +  getIssue().getNumber() + " - " + getProject().getPath();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueDetailCssResourceReference()));
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project, 0));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}

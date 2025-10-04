package io.onedev.server.web.page.project.issues.detail;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.issue.editabletitle.IssueEditableTitlePanel;
import io.onedev.server.web.component.issue.operation.IssueOperationsPanel;
import io.onedev.server.web.component.issue.primary.IssuePrimaryPanel;
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
import io.onedev.server.xodus.VisitInfoService;

public abstract class IssueDetailPage extends ProjectIssuesPage implements InputContext {

	public static final String PARAM_ISSUE = "issue";
	
	private static final String KEY_SCROLL_TOP = "onedev.issue.scrollTop";

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
					throw new ValidationException(MessageFormat.format(_T("Invalid issue number: {0}"), issueNumberString));
				}
				
				Issue issue = getIssueService().find(getProject(), issueNumber);
				if (issue == null) { 
					throw new EntityNotFoundException(MessageFormat.format(_T("Unable to find issue #{0} in project {1}"), issueNumber, getProject()));
				} else {
					OneDev.getInstance(IssueLinkService.class).loadDeepLinks(issue);
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
		return SecurityUtils.canAccessIssue(getIssue());
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
		
		add(new IssuePrimaryPanel("primary") {

			@Override
			protected Issue getIssue() {
				return IssueDetailPage.this.getIssue();
			}
		});
		
		add(new Tabbable("issueTabs", new LoadableDetachableModel<>() {
			@Override
			protected List<? extends Tab> load() {
				List<Tab> tabs = new ArrayList<>();
				tabs.add(new PageTab(Model.of(_T("Activities")), IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(getIssue())) {

					@Override
					public Component render(String id) {
						return new PageTabHead(id, this) {
		
							@Override
							protected Link<?> newLink(String id, Class<? extends Page> pageClass, PageParameters pageParams) {
								return new ViewStateAwarePageLink<Void>(id, pageClass, pageParams, KEY_SCROLL_TOP);
							}
		
						};
					}
		
					@Override
					protected Component renderOptions(String id) {
						IssueActivitiesPage page = (IssueActivitiesPage) getPage();
						return page.renderOptions(id);
					}

				});

				if (!getIssue().getFixCommits(false).isEmpty()) {
					if (SecurityUtils.canReadCode(getProject())) {
						tabs.add(new PageTab(Model.of(_T("Fixing Commits")), IssueCommitsPage.class, IssueCommitsPage.paramsOf(getIssue())) {

							@Override
							public Component render(String componentId) {
								return new PageTabHead(componentId, this) {
				
									@Override
									protected Link<?> newLink(String componentId, Class<? extends Page> pageClass, PageParameters pageParams) {
										return new ViewStateAwarePageLink<Void>(componentId, pageClass, pageParams, KEY_SCROLL_TOP);
									}
				
								};
							}

						});
						if (!getIssue().getPullRequests().isEmpty()) {
							tabs.add(new PageTab(Model.of(_T("Pull Requests")), IssuePullRequestsPage.class, IssuePullRequestsPage.paramsOf(getIssue())) {

								@Override
								public Component render(String componentId) {
									return new PageTabHead(componentId, this) {
					
										@Override
										protected Link<?> newLink(String componentId, Class<? extends Page> pageClass, PageParameters pageParams) {
											return new ViewStateAwarePageLink<Void>(componentId, pageClass, pageParams, KEY_SCROLL_TOP);
										}
					
									};
								}
	
							});
						}
					}
					// Do not calculate fix builds now as it might be slow
					tabs.add(new PageTab(Model.of(_T("Fixing Builds")), IssueBuildsPage.class, IssueBuildsPage.paramsOf(getIssue())) {

						@Override
						public Component render(String componentId) {
							return new PageTabHead(componentId, this) {
			
								@Override
								protected Link<?> newLink(String componentId, Class<? extends Page> pageClass, PageParameters pageParams) {
									return new ViewStateAwarePageLink<Void>(componentId, pageClass, pageParams, KEY_SCROLL_TOP);
								}
			
							};
						}

					});
				}

				if (getIssue().isConfidential() && SecurityUtils.canModifyIssue(getIssue())) {
					tabs.add(new PageTab(Model.of(_T("Authorizations")), IssueAuthorizationsPage.class, IssueAuthorizationsPage.paramsOf(getIssue())) {

						@Override
						public Component render(String componentId) {
							return new PageTabHead(componentId, this) {
			
								@Override
								protected Link<?> newLink(String componentId, Class<? extends Page> pageClass, PageParameters pageParams) {
									return new ViewStateAwarePageLink<Void>(componentId, pageClass, pageParams, KEY_SCROLL_TOP);
								}
			
							};
						}
					
					});
				}

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
								getIssueService().delete(getIssue());
								var oldAuditContent = VersionedXmlDoc.fromBean(getIssue()).toXML();
								auditService.audit(getIssue().getProject(), "deleted issue \"" + getIssue().getReference().toString(getIssue().getProject()) + "\"", oldAuditContent, null);
								
								Session.get().success(MessageFormat.format(_T("Issue #{0} deleted"), getIssue().getNumber()));
								
								String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Issue.class);
								if (redirectUrlAfterDelete != null)
									throw new RedirectToUrlException(redirectUrlAfterDelete);
								else
									setResponsePage(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(getProject()));
							}
							
						}.add(new ConfirmClickModifier(_T("Do you really want to delete this issue?")));
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
						return getIssueService().query(SecurityUtils.getSubject(), projectScope, query, false, offset, count);
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
		
		RequestCycle.get().getListeners().add(new AbstractRequestCycleListener() {
						
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getAuthUser() != null) 
					OneDev.getInstance(VisitInfoService.class).visitIssue(SecurityUtils.getAuthUser(), getIssue());
			}
						
		});	

	}
	
	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Issue issue) {
		return paramsOf(issue.getProject(), issue.getNumber());
	}

	public static PageParameters paramsOf(Project project, Long issueNumber) {
		PageParameters params = ProjectPage.paramsOf(project);
		params.add(PARAM_ISSUE, issueNumber);
		return params;
	}
	
	@Override
	public List<String> getInputNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldSpec getInputSpec(String inputName) {
		return OneDev.getInstance(SettingService.class).getIssueSetting().getFieldSpec(inputName);
	}
	
	private IssueService getIssueService() {
		return OneDev.getInstance(IssueService.class);
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("issues", ProjectIssueListPage.class, 
				ProjectIssueListPage.paramsOf(getProject(), 0)));
		fragment.add(new Label("issueNumber", getIssue().getReference().toString(getProject())));
		return fragment;
	}

	@Override
	protected String getPageTitle() {
		return getIssue().getTitle() + " (" + getIssue().getReference().toString(getProject()) + ")";
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueDetailResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(String.format( "onedev.server.issueDetail.onDomReady('%s');", KEY_SCROLL_TOP)));
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project, 0));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}

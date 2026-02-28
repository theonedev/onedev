package io.onedev.server.web.page.project.workspaces;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.WorkspaceQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.administration.GlobalWorkspaceSetting;
import io.onedev.server.model.support.workspace.NamedWorkspaceQuery;
import io.onedev.server.model.support.workspace.ProjectWorkspaceSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.workspace.list.WorkspaceListPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.NamedWorkspaceQueriesBean;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;
import io.onedev.server.workspace.WorkspaceQueryPersonalizationService;

public class ProjectWorkspacesPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";

	private static final String PARAM_QUERY = "query";

	private String query;

	private SavedQueriesPanel<NamedWorkspaceQuery> savedQueries;

	private WorkspaceListPanel workspaces;

	public ProjectWorkspacesPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toOptionalString();
	}

	private WorkspaceQueryPersonalizationService getWorkspaceQueryPersonalizationService() {
		return OneDev.getInstance(WorkspaceQueryPersonalizationService.class);
	}

	protected GlobalWorkspaceSetting getWorkspaceSetting() {
		return OneDev.getInstance(SettingService.class).getWorkspaceSetting();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canWriteCode(getProject());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedWorkspaceQuery> newNamedQueriesBean() {
				return new NamedWorkspaceQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedWorkspaceQuery namedQuery) {
				return new BookmarkablePageLink<>(componentId, ProjectWorkspacesPage.class,
						ProjectWorkspacesPage.paramsOf(getProject(), namedQuery.getQuery(), 0));
			}

			@Override
			protected QueryPersonalization<NamedWorkspaceQuery> getQueryPersonalization() {
				return getProject().getWorkspaceQueryPersonalizationOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedWorkspaceQuery> getCommonQueries() {
				return (ArrayList<NamedWorkspaceQuery>) getProject().getWorkspaceSetting().getNamedQueries();
			}

			private String getAuditContent() {
				var auditData = getProject().getWorkspaceSetting().getNamedQueries();
				if (auditData == null)
					auditData = getWorkspaceSetting().getNamedQueries();
				return VersionedXmlDoc.fromBean(auditData).toXML();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedWorkspaceQuery> namedQueries) {
				var oldAuditContent = getAuditContent();
				getProject().getWorkspaceSetting().setNamedQueries(namedQueries);
				var newAuditContent = getAuditContent();
				getProjectService().update(getProject());
				auditService.audit(getProject(), "changed workspace queries", oldAuditContent, newAuditContent);
			}

			@Override
			protected ArrayList<NamedWorkspaceQuery> getInheritedCommonQueries() {
				if (getProject().getParent() != null)
					return (ArrayList<NamedWorkspaceQuery>) getProject().getParent().getNamedWorkspaceQueries();
				else
					return (ArrayList<NamedWorkspaceQuery>) getWorkspaceSetting().getNamedQueries();
			}

		});

		add(workspaces = new WorkspaceListPanel("workspaces", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				CharSequence url = RequestCycle.get().urlFor(ProjectWorkspacesPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}

		}) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new ParamPagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						return paramsOf(getProject(), query, currentPage + 1);
					}

					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_PAGE).toInt(1) - 1;
					}

				};
			}

			@Override
			protected QuerySaveSupport getQuerySaveSupport() {
				return new QuerySaveSupport() {

					@Override
					public void onSaveQuery(AjaxRequestTarget target, String query) {
						new ModalPanel(target) {

							@Override
							protected Component newContent(String id) {
								return new SaveQueryPanel(id, new PersonalQuerySupport() {

									@Override
									public void onSave(AjaxRequestTarget target, String name) {
										WorkspaceQueryPersonalization setting =
												getProject().getWorkspaceQueryPersonalizationOfCurrentUser();
										NamedWorkspaceQuery namedQuery = NamedQuery.find(setting.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedWorkspaceQuery(name, query);
											setting.getQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getWorkspaceQueryPersonalizationService().createOrUpdate(setting);
										target.add(savedQueries);
										close();
									}

								}) {

								@Override
								protected void onSave(AjaxRequestTarget target, String name) {
									ProjectWorkspaceSetting setting = getProject().getWorkspaceSetting();
									if (setting.getNamedQueries() == null)
										setting.setNamedQueries(new ArrayList<>(getWorkspaceSetting().getNamedQueries()));
									NamedWorkspaceQuery namedQuery = getProject().getNamedWorkspaceQuery(name);
									String oldAuditContent = null;
									String verb;
									if (namedQuery == null) {
										namedQuery = new NamedWorkspaceQuery(name, query);
										setting.getNamedQueries().add(namedQuery);
										verb = "created";
									} else {
										oldAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
										namedQuery.setQuery(query);
										verb = "changed";
									}
									var newAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
									getProjectService().update(getProject());
									auditService.audit(getProject(),
											verb + " workspace query \"" + name + "\"",
											oldAuditContent, newAuditContent);
									target.add(savedQueries);
									close();
								}

									@Override
									protected void onCancel(AjaxRequestTarget target) {
										close();
									}

								};
							}

						};
					}

					@Override
					public boolean isSavedQueriesVisible() {
						savedQueries.configure();
						return savedQueries.isVisible();
					}

				};
			}

			@Override
			protected Project getProject() {
				return ProjectWorkspacesPage.this.getProject();
			}

		});
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(workspaces);
	}

	public static PageParameters paramsOf(Project project, @Nullable String query, int page) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}

	public static PageParameters paramsOf(Project project, int page) {
		String query = null;
		if (project.getWorkspaceQueryPersonalizationOfCurrentUser() != null
				&& !project.getWorkspaceQueryPersonalizationOfCurrentUser().getQueries().isEmpty()) {
			query = project.getWorkspaceQueryPersonalizationOfCurrentUser().getQueries()
					.iterator().next().getQuery();
		} else if (!project.getNamedWorkspaceQueries().isEmpty()) {
			query = project.getNamedWorkspaceQueries().iterator().next().getQuery();
		}
		return paramsOf(project, query, page);
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		return new ViewStateAwarePageLink<>(componentId, ProjectDashboardPage.class,
				ProjectDashboardPage.paramsOf(project.getId()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Workspaces"));
	}

	@Override
	protected String getPageTitle() {
		return _T("Workspaces") + " - " + getProject().getPath();
	}

}

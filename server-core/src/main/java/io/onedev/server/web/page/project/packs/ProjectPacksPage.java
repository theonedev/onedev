package io.onedev.server.web.page.project.packs;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.PackQueryPersonalizationService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.PackQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.administration.GlobalPackSetting;
import io.onedev.server.model.support.pack.NamedPackQuery;
import io.onedev.server.model.support.pack.ProjectPackSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.pack.list.PackListPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.NamedPackQueriesBean;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class ProjectPacksPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedPackQuery> savedQueries;
	
	private PackListPanel packList;
	
	public ProjectPacksPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toOptionalString();
	}

	private PackQueryPersonalizationService getPackQueryPersonalizationService() {
		return OneDev.getInstance(PackQueryPersonalizationService.class);
	}
	
	protected GlobalPackSetting getPackSetting() {
		return OneDev.getInstance(SettingService.class).getPackSetting();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadPack(getProject());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedPackQuery> newNamedQueriesBean() {
				return new NamedPackQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedPackQuery namedQuery) {
				return new BookmarkablePageLink<>(componentId, ProjectPacksPage.class,
						ProjectPacksPage.paramsOf(getProject(), namedQuery.getQuery(), 0));
			}

			@Override
			protected QueryPersonalization<NamedPackQuery> getQueryPersonalization() {
				return getProject().getPackQueryPersonalizationOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedPackQuery> getCommonQueries() {
				return (ArrayList<NamedPackQuery>) getProject().getPackSetting().getNamedQueries();
			}

			private String getAuditContent() {
				var auditData = getProject().getPackSetting().getNamedQueries();
				if (auditData == null) 
					auditData = getPackSetting().getNamedQueries();
				return VersionedXmlDoc.fromBean(auditData).toXML();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedPackQuery> namedQueries) {
				var oldAuditContent = getAuditContent();
				getProject().getPackSetting().setNamedQueries(namedQueries);
				var newAuditContent = getAuditContent();
				getProjectService().update(getProject());
				auditService.audit(getProject(), "changed package queries", oldAuditContent, newAuditContent);
			}

			@Override
			protected ArrayList<NamedPackQuery> getInheritedCommonQueries() {
				if (getProject().getParent() != null)
					return (ArrayList<NamedPackQuery>) getProject().getParent().getNamedPackQueries();
				else
					return (ArrayList<NamedPackQuery>) getPackSetting().getNamedQueries();
			}

		});
		
		add(packList = new PackListPanel("packs", new IModel<>() {

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
				CharSequence url = RequestCycle.get().urlFor(ProjectPacksPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}

		}, true) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new ParamPagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						return paramsOf(getProject(), query, currentPage+1);
					}
					
					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
					}
					
				};
			}

			@Override
			protected QuerySaveSupport getQuerySaveSupport() {
				return new QuerySaveSupport() {

					@Override
					public void onSaveQuery(AjaxRequestTarget target, String query) {
						new ModalPanel(target)  {

							@Override
							protected Component newContent(String id) {
								return new SaveQueryPanel(id, new PersonalQuerySupport() {
									@Override
									public void onSave(AjaxRequestTarget target, String name) {
										PackQueryPersonalization setting = getProject().getPackQueryPersonalizationOfCurrentUser();
										NamedPackQuery namedQuery = NamedQuery.find(setting.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedPackQuery(name, query);
											setting.getQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getPackQueryPersonalizationService().createOrUpdate(setting);

										target.add(savedQueries);
										close();
									}
								}) {

									@Override
									protected void onSave(AjaxRequestTarget target, String name) {
										ProjectPackSetting setting = getProject().getPackSetting();
										if (setting.getNamedQueries() == null) 
											setting.setNamedQueries(new ArrayList<>(getPackSetting().getNamedQueries()));
										NamedPackQuery namedQuery = getProject().getNamedPackQuery(name);
										String oldAuditContent = null;
										String verb;
										if (namedQuery == null) {
											namedQuery = new NamedPackQuery(name, query);
											setting.getNamedQueries().add(namedQuery);	
											verb = "created";
										} else {
											oldAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
											namedQuery.setQuery(query);
											verb = "changed";
										}
										var newAuditContent = VersionedXmlDoc.fromBean(namedQuery).toXML();
										getProjectService().update(getProject());
										auditService.audit(getProject(), verb + " package query \"" + name + "\"", oldAuditContent, newAuditContent);
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
				return ProjectPacksPage.this.getProject();
			}

		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(packList);
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
		if (project.getPackQueryPersonalizationOfCurrentUser() != null 
				&& !project.getPackQueryPersonalizationOfCurrentUser().getQueries().isEmpty()) {
			query = project.getPackQueryPersonalizationOfCurrentUser().getQueries().iterator().next().getQuery();
		} else if (!project.getNamedPackQueries().isEmpty()) {
			query = project.getNamedPackQueries().iterator().next().getQuery();
		}
		return paramsOf(project, query, page);
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectPacksPage.class, ProjectPacksPage.paramsOf(project, 0));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Packages"));
	}
	
	@Override
	protected String getPageTitle() {
		return _T("Packages") + " - " + getProject().getPath();
	}
	
}

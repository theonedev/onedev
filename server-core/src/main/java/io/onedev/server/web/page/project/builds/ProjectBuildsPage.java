package io.onedev.server.web.page.project.builds;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.NamedBuildQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectBuildsPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedBuildQuery> savedQueries;
	
	private BuildListPanel buildList;
	
	public ProjectBuildsPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toOptionalString();
	}

	private BuildQuerySettingManager getBuildQuerySettingManager() {
		return OneDev.getInstance(BuildQuerySettingManager.class);		
	}
	
	protected GlobalBuildSetting getBuildSetting() {
		return OneDev.getInstance(SettingManager.class).getBuildSetting();		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<NamedBuildQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedBuildQuery> newNamedQueriesBean() {
				return new NamedBuildQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedBuildQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, ProjectBuildsPage.class, 
						ProjectBuildsPage.paramsOf(getProject(), namedQuery.getQuery(), 0));
			}

			@Override
			protected QuerySetting<NamedBuildQuery> getQuerySetting() {
				return getProject().getBuildQuerySettingOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedBuildQuery> getQueries() {
				return (ArrayList<NamedBuildQuery>) getProject().getBuildSetting().getNamedQueries(false);
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedBuildQuery> querySetting) {
				getBuildQuerySettingManager().save((BuildQuerySetting) querySetting);
			}

			@Override
			protected void onSaveQueries(ArrayList<NamedBuildQuery> namedQueries) {
				getProject().getBuildSetting().setNamedQueries(namedQueries);
				OneDev.getInstance(ProjectManager.class).save(getProject());
			}

			@Override
			protected ArrayList<NamedBuildQuery> getDefaultQueries() {
				return (ArrayList<NamedBuildQuery>) getBuildSetting().getNamedQueries();
			}

		});
		
		add(buildList = new BuildListPanel("builds", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(ProjectBuildsPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}, 0) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

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
								return new SaveQueryPanel(id) {

									@Override
									protected void onSaveForMine(AjaxRequestTarget target, String name) {
										BuildQuerySetting setting = getProject().getBuildQuerySettingOfCurrentUser();
										NamedBuildQuery namedQuery = NamedQuery.find(setting.getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedBuildQuery(name, query);
											setting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getBuildQuerySettingManager().save(setting);
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										ProjectBuildSetting setting = getProject().getBuildSetting();
										if (setting.getNamedQueries(false) == null) 
											setting.setNamedQueries(new ArrayList<>(getBuildSetting().getNamedQueries()));
										NamedBuildQuery namedQuery = getProject().getBuildSetting().getNamedQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedBuildQuery(name, query);
											setting.getNamedQueries(false).add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(ProjectManager.class).save(getProject());
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
				return ProjectBuildsPage.this.getProject();
			}

		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(buildList);
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
		if (project.getBuildQuerySettingOfCurrentUser() != null 
				&& !project.getBuildQuerySettingOfCurrentUser().getUserQueries().isEmpty()) {
			query = project.getBuildQuerySettingOfCurrentUser().getUserQueries().iterator().next().getQuery();
		} else if (!project.getBuildSetting().getNamedQueries(true).isEmpty()) {
			query = project.getBuildSetting().getNamedQueries(true).iterator().next().getQuery();
		}
		return paramsOf(project, query, page);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Builds");
	}
	
	@Override
	protected String getPageTitle() {
		return "Builds - " + getProject().getName();
	}
	
}

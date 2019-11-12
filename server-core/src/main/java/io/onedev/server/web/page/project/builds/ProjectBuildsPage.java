package io.onedev.server.web.page.project.builds;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedBuildQuery;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.ProjectBuildSetting;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.administration.BuildSetting;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectBuildsPage extends ProjectPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	public ProjectBuildsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
		if (query != null && query.length() == 0) {
			List<String> queries = new ArrayList<>();
			if (getProject().getBuildQuerySettingOfCurrentUser() != null) { 
				for (NamedBuildQuery namedQuery: getProject().getBuildQuerySettingOfCurrentUser().getUserQueries())
					queries.add(namedQuery.getQuery());
			}
			for (NamedBuildQuery namedQuery: getProject().getBuildSetting().getNamedQueries(true))
				queries.add(namedQuery.getQuery());
			query = null;
			for (String each: queries) {
				try {
					if (SecurityUtils.getUser() != null || !BuildQuery.parse(getProject(), each).needsLogin()) {  
						query = each;
						break;
					}
				} catch (Exception e) {
				}
			} 
		}
	}

	private BuildQuerySettingManager getBuildQuerySettingManager() {
		return OneDev.getInstance(BuildQuerySettingManager.class);		
	}
	
	protected BuildSetting getBuildSetting() {
		return OneDev.getInstance(SettingManager.class).getBuildSetting();		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		SavedQueriesPanel<NamedBuildQuery> savedQueries;
		add(savedQueries = new SavedQueriesPanel<NamedBuildQuery>("side") {

			@Override
			protected NamedQueriesBean<NamedBuildQuery> newNamedQueriesBean() {
				return new NamedBuildQueriesBean();
			}

			@Override
			protected boolean needsLogin(NamedBuildQuery namedQuery) {
				return BuildQuery.parse(getProject(), namedQuery.getQuery()).needsLogin();
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
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), query, 0);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(new BuildListPanel("main", getProject(), query) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				setResponsePage(ProjectBuildsPage.class, paramsOf(getProject(), query, 0));
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

		});
		
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query, int page) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_CURRENT_PAGE, page);
		return params;
	}
	
}

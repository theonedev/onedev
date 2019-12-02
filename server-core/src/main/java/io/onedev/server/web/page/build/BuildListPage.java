package io.onedev.server.web.page.build;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedBuildQuery;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.NamedBuildQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class BuildListPage extends LayoutPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_EXPECTED_COUNT = "expectedCount";
	
	private int expectedCount;
	
	private final IModel<String> queryModel = new LoadableDetachableModel<String>() {

		@Override
		protected String load() {
			String query = getPageParameters().get(PARAM_QUERY).toOptionalString();
			if (query != null && query.length() == 0) {
				query = null;
				List<String> queries = new ArrayList<>();
				if (getLoginUser() != null) {
					for (NamedBuildQuery namedQuery: getLoginUser().getBuildQuerySetting().getUserQueries())
						queries.add(namedQuery.getQuery());
				}
				for (NamedBuildQuery namedQuery: getBuildSetting().getNamedQueries())
					queries.add(namedQuery.getQuery());
				for (String each: queries) {
					try {
						if (SecurityUtils.getUser() != null || !BuildQuery.parse(null, each).needsLogin()) {  
							query = each;
							break;
						}
					} catch (Exception e) {
					}
				} 
			}
			return query;
		}
		
	};
	
	public BuildListPage(PageParameters params) {
		super(params);
		expectedCount = params.get(PARAM_EXPECTED_COUNT).toInt(0);
	}

	protected GlobalBuildSetting getBuildSetting() {
		return OneDev.getInstance(SettingManager.class).getBuildSetting();		
	}
	
	@Override
	protected void onDetach() {
		queryModel.detach();
		super.onDetach();
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
				return BuildQuery.parse(null, namedQuery.getQuery()).needsLogin();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedBuildQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, BuildListPage.class, 
						BuildListPage.paramsOf(namedQuery.getQuery(), 0, 0));
			}

			@Override
			protected QuerySetting<NamedBuildQuery> getQuerySetting() {
				if (getLoginUser() != null)
					return getLoginUser().getBuildQuerySetting();
				else
					return null;
			}

			@Override
			protected ArrayList<NamedBuildQuery> getQueries() {
				return (ArrayList<NamedBuildQuery>) getBuildSetting().getNamedQueries();
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedBuildQuery> querySetting) {
				OneDev.getInstance(UserManager.class).save(getLoginUser());
			}

			@Override
			protected void onSaveQueries(ArrayList<NamedBuildQuery> namedQueries) {
				getBuildSetting().setNamedQueries(namedQueries);
				OneDev.getInstance(SettingManager.class).saveBuildSetting(getBuildSetting());
			}

		});
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(queryModel.getObject(), 0, 0);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(new BuildListPanel("main", queryModel.getObject(), expectedCount) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				setResponsePage(BuildListPage.class, paramsOf(query, 0, 0));
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
										QuerySetting<NamedBuildQuery> querySetting = getLoginUser().getBuildQuerySetting();
										NamedBuildQuery namedQuery = NamedQuery.find(querySetting.getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedBuildQuery(name, query);
											querySetting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(UserManager.class).save(getLoginUser());
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										GlobalBuildSetting buildSetting = getBuildSetting();
										NamedBuildQuery namedQuery = buildSetting.getNamedQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedBuildQuery(name, query);
											buildSetting.getNamedQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(SettingManager.class).saveBuildSetting(buildSetting);
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
				return null;
			}

		});
		
	}
	
	public static PageParameters paramsOf(@Nullable String query, int page, int expectedCount) {
		PageParameters params = new PageParameters();
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_CURRENT_PAGE, page);
		if (expectedCount != 0)
			params.add(PARAM_EXPECTED_COUNT, expectedCount);
		return params;
	}
	
}

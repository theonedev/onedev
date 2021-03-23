package io.onedev.server.web.page.builds;

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
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.security.SecurityUtils;
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

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_EXPECTED_COUNT = "expectedCount";
	
	private String query;
	
	private final int expectedCount;
	
	private SavedQueriesPanel<NamedBuildQuery> savedQueries;
	
	private BuildListPanel buildList;
	
	public BuildListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
		expectedCount = params.get(PARAM_EXPECTED_COUNT).toInt(0);
		params.remove(PARAM_EXPECTED_COUNT);
	}

	private static GlobalBuildSetting getBuildSetting() {
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
				CharSequence url = RequestCycle.get().urlFor(BuildListPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}, expectedCount) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						return paramsOf(query, currentPage+1, 0);
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
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(buildList);
	}
	
	public static PageParameters paramsOf(@Nullable String query, int page, int expectedCount) {
		PageParameters params = new PageParameters();
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		if (expectedCount != 0)
			params.add(PARAM_EXPECTED_COUNT, expectedCount);
		return params;
	}

	public static PageParameters paramsOf(int page, int expectedCount) {
		String query = null;
		User user = SecurityUtils.getUser();
		if (user != null && !user.getBuildQuerySetting().getUserQueries().isEmpty()) 
			query = user.getBuildQuerySetting().getUserQueries().iterator().next().getQuery();
		else if (!getBuildSetting().getNamedQueries().isEmpty())
			query = getBuildSetting().getNamedQueries().iterator().next().getQuery();
		
		return paramsOf(query, page, expectedCount);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Builds");
	}
	
	@Override
	protected String getPageTitle() {
		return "Builds - OneDev";
	}
	
}

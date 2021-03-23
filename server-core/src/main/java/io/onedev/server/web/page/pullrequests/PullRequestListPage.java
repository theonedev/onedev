package io.onedev.server.web.page.pullrequests;

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
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.pullrequest.list.PullRequestListPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.NamedPullRequestQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class PullRequestListPage extends LayoutPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedPullRequestQuery> savedQueries;
	
	private PullRequestListPanel requestList;
	
	public PullRequestListPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toOptionalString();
	}
	
	private static GlobalPullRequestSetting getPullRequestSetting() {
		return OneDev.getInstance(SettingManager.class).getPullRequestSetting();		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<NamedPullRequestQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedPullRequestQuery> newNamedQueriesBean() {
				return new NamedPullRequestQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedPullRequestQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, PullRequestListPage.class, 
						PullRequestListPage.paramsOf(namedQuery.getQuery(), 0));
			}

			@Override
			protected QuerySetting<NamedPullRequestQuery> getQuerySetting() {
				if (getLoginUser() != null)
					return getLoginUser().getPullRequestQuerySetting();
				else
					return null;
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedPullRequestQuery> querySetting) {
				OneDev.getInstance(UserManager.class).save(getLoginUser());
			}

			@Override
			protected void onSaveQueries(ArrayList<NamedPullRequestQuery> queries) {
				getPullRequestSetting().setNamedQueries(queries);
				OneDev.getInstance(SettingManager.class).savePullRequestSetting(getPullRequestSetting());
			}

			@Override
			protected ArrayList<NamedPullRequestQuery> getQueries() {
				return (ArrayList<NamedPullRequestQuery>) getPullRequestSetting().getNamedQueries();
			}

		});

		add(requestList = new PullRequestListPanel("pullRequests", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(PullRequestListPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(query, 0);
						params.add(PARAM_PAGE, currentPage+1);
						return params;
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
										QuerySetting<NamedPullRequestQuery> querySetting = getLoginUser().getPullRequestQuerySetting();
										NamedPullRequestQuery namedQuery = NamedQuery.find(querySetting.getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedPullRequestQuery(name, query);
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
										GlobalPullRequestSetting pullRequestSetting = getPullRequestSetting();
										NamedPullRequestQuery namedQuery = pullRequestSetting.getNamedQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedPullRequestQuery(name, query);
											pullRequestSetting.getNamedQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(SettingManager.class).savePullRequestSetting(pullRequestSetting);
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
		target.add(requestList);
	}
	
	public static PageParameters paramsOf(@Nullable String query, int page) {
		PageParameters params = new PageParameters();
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}
	
	@Override
	protected String getPageTitle() {
		return "Pull Requests - OneDev";
	}
	
	public static PageParameters paramsOf(int page) {
		String query = null;
		User user = SecurityUtils.getUser();
		if (user != null && !user.getPullRequestQuerySetting().getUserQueries().isEmpty())
			query = user.getPullRequestQuerySetting().getUserQueries().iterator().next().getQuery();
		else if (!getPullRequestSetting().getNamedQueries().isEmpty())
			query = getPullRequestSetting().getNamedQueries().iterator().next().getQuery();
		return paramsOf(query, page);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Pull Requests");
	}
	
}

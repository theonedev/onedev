package io.onedev.server.web.page.issue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.NamedIssueQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class IssueListPage extends LayoutPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	public IssueListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
		if (query != null && query.length() == 0) {
			query = null;
			List<String> queries = new ArrayList<>();
			if (getLoginUser() != null) {
				for (NamedIssueQuery namedQuery: getLoginUser().getIssueQuerySetting().getUserQueries())
					queries.add(namedQuery.getQuery());
			}
			for (NamedIssueQuery namedQuery: getIssueSetting().getNamedQueries())
				queries.add(namedQuery.getQuery());
			for (String each: queries) {
				try {
					if (SecurityUtils.getUser() != null || !IssueQuery.parse(null, each, true).needsLogin()) {  
						query = each;
						break;
					}
				} catch (Exception e) {
				}
			} 
		}
	}
	
	protected IssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();		
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
		
		SavedQueriesPanel<NamedIssueQuery> savedQueries;
		add(savedQueries = new SavedQueriesPanel<NamedIssueQuery>("side") {

			@Override
			protected NamedQueriesBean<NamedIssueQuery> newNamedQueriesBean() {
				return new NamedIssueQueriesBean();
			}

			@Override
			protected boolean needsLogin(NamedIssueQuery namedQuery) {
				return IssueQuery.parse(null, namedQuery.getQuery(), true).needsLogin();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedIssueQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, IssueListPage.class, 
						IssueListPage.paramsOf(namedQuery.getQuery(), 0));
			}

			@Override
			protected QuerySetting<NamedIssueQuery> getQuerySetting() {
				if (getLoginUser() != null)
					return getLoginUser().getIssueQuerySetting();
				else
					return null;
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedIssueQuery> querySetting) {
				getLoginUser().setIssueQuerySetting(querySetting);
				OneDev.getInstance(UserManager.class).save(getLoginUser());
			}

			@Override
			protected void onSaveQueries(ArrayList<NamedIssueQuery> queries) {
				getIssueSetting().setNamedQueries(queries);
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getIssueSetting());
			}

			@Override
			protected ArrayList<NamedIssueQuery> getQueries() {
				return (ArrayList<NamedIssueQuery>) getIssueSetting().getNamedQueries();
			}
			
		});
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(query, 0);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(new IssueListPanel("main", null, query) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				setResponsePage(IssueListPage.class, paramsOf(query, 0));
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
										NamedIssueQuery namedQuery = NamedQuery.find(getLoginUser().getIssueQuerySetting().getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											getLoginUser().getIssueQuerySetting().getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(UserManager.class).save(getLoginUser());
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										IssueSetting issueSetting = getIssueSetting();
										NamedIssueQuery namedQuery = issueSetting.getNamedQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											issueSetting.getNamedQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(SettingManager.class).saveIssueSetting(issueSetting);
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

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListCssResourceReference()));
	}

	public static PageParameters paramsOf(@Nullable String query, int page) {
		PageParameters params = new PageParameters();
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_CURRENT_PAGE, page);
		return params;
	}
	
}

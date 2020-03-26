package io.onedev.server.web.page.issue;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.NamedIssueQuery;
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

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedIssueQuery> savedQueries;
	
	public IssueListPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toOptionalString();
	}
	
	private static GlobalIssueSetting getIssueSetting() {
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
		
		add(savedQueries = new SavedQueriesPanel<NamedIssueQuery>("side") {

			@Override
			protected NamedQueriesBean<NamedIssueQuery> newNamedQueriesBean() {
				return new NamedIssueQueriesBean();
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
		
		add(newIssueList());
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		IssueListPanel listPanel = newIssueList();
		replace(listPanel);
		target.add(listPanel);
	}
	
	private IssueListPanel newIssueList() {
		return new IssueListPanel("main", query) {

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
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				CharSequence url = RequestCycle.get().urlFor(IssueListPage.class, paramsOf(query, 0));
				IssueListPage.this.query = query;
				pushState(target, url.toString(), query);
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
										QuerySetting<NamedIssueQuery> querySetting = getLoginUser().getIssueQuerySetting();
										NamedIssueQuery namedQuery = NamedQuery.find(querySetting.getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
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
										GlobalIssueSetting issueSetting = getIssueSetting();
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

			@Override
			protected Project getProject() {
				return null;
			}

		};				
	}
	
	public static PageParameters paramsOf(@Nullable String query, int page) {
		PageParameters params = new PageParameters();
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}
	
	public static PageParameters paramsOf(int page) {
		String query = null;
		User user = SecurityUtils.getUser();
		if (user != null && !user.getIssueQuerySetting().getUserQueries().isEmpty()) 
			query = user.getIssueQuerySetting().getUserQueries().iterator().next().getQuery();
		else if (!getIssueSetting().getNamedQueries().isEmpty())
			query = getIssueSetting().getNamedQueries().iterator().next().getQuery();
		
		return paramsOf(query, page);
	}
	
}

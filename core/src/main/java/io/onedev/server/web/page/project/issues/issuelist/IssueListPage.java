package io.onedev.server.web.page.project.issues.issuelist;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entityquery.issue.IssueQuery;
import io.onedev.server.manager.IssueQuerySettingManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.web.component.issuelist.IssueListPanel;
import io.onedev.server.web.component.issuelist.QuerySaveSupport;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.issues.IssuesPage;
import io.onedev.server.web.page.project.savedquery.NamedQueriesBean;
import io.onedev.server.web.page.project.savedquery.SaveQueryPanel;
import io.onedev.server.web.page.project.savedquery.SavedQueriesPanel;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class IssueListPage extends IssuesPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	public IssueListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private IssueQuerySettingManager getIssueQuerySettingManager() {
		return OneDev.getInstance(IssueQuerySettingManager.class);		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Component side;
		add(side = new SavedQueriesPanel<NamedIssueQuery>("side") {

			@Override
			protected NamedQueriesBean<NamedIssueQuery> newNamedQueriesBean() {
				return new NamedIssueQueriesBean();
			}

			@Override
			protected boolean needsLogin(NamedIssueQuery namedQuery) {
				return IssueQuery.parse(getProject(), namedQuery.getQuery(), true).needsLogin();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedIssueQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, IssueListPage.class, IssueListPage.paramsOf(getProject(), namedQuery.getQuery()));
			}

			@Override
			protected QuerySetting<NamedIssueQuery> getQuerySetting() {
				return getProject().getIssueQuerySettingOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedIssueQuery> getProjectQueries() {
				return getProject().getSavedIssueQueries();
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedIssueQuery> querySetting) {
				getIssueQuerySettingManager().save((IssueQuerySetting) querySetting);
			}

			@Override
			protected void onSaveProjectQueries(ArrayList<NamedIssueQuery> projectQueries) {
				getProject().setSavedIssueQueries(projectQueries);
				OneDev.getInstance(ProjectManager.class).save(getProject());
			}
			
		});
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), query);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(new IssueListPanel("main", new PropertyModel<String>(this, "query")) {

			@Override
			protected Project getProject() {
				return IssueListPage.this.getProject();
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target) {
				setResponsePage(IssueListPage.class, paramsOf(getProject(), query));
			}

			@Override
			protected QuerySaveSupport getQuerySaveSupport() {
				return new QuerySaveSupport() {

					@Override
					public void onSaveQuery(AjaxRequestTarget target) {
						new ModalPanel(target)  {

							@Override
							protected Component newContent(String id) {
								return new SaveQueryPanel(id) {

									@Override
									protected void onSaveForMine(AjaxRequestTarget target, String name) {
										IssueQuerySetting setting = getProject().getIssueQuerySettingOfCurrentUser();
										NamedIssueQuery namedQuery = setting.getUserQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											setting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getIssueQuerySettingManager().save(setting);
										target.add(side);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										NamedIssueQuery namedQuery = getProject().getSavedIssueQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											getProject().getSavedIssueQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(ProjectManager.class).save(getProject());
										target.add(side);
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
					
				};
			}

		});
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListResourceReference()));
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}

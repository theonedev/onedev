package io.onedev.server.web.page.project.comments;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentQuerySettingManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.CodeCommentQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.codecomment.NamedCodeCommentQuery;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.codecomment.CodeCommentListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.savedquery.NamedQueriesBean;
import io.onedev.server.web.page.project.savedquery.SaveQueryPanel;
import io.onedev.server.web.page.project.savedquery.SavedQueriesPanel;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectCodeCommentsPage extends ProjectPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private final String query;
	
	public ProjectCodeCommentsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	private CodeCommentQuerySettingManager getCodeCommentQuerySettingManager() {
		return OneDev.getInstance(CodeCommentQuerySettingManager.class);		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Component side;
		add(side = new SavedQueriesPanel<NamedCodeCommentQuery>("side") {

			@Override
			protected NamedQueriesBean<NamedCodeCommentQuery> newNamedQueriesBean() {
				return new NamedCodeCommentQueriesBean();
			}

			@Override
			protected boolean needsLogin(NamedCodeCommentQuery namedQuery) {
				return CodeCommentQuery.parse(getProject(), namedQuery.getQuery(), true).needsLogin();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedCodeCommentQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, ProjectCodeCommentsPage.class, ProjectCodeCommentsPage.paramsOf(getProject(), namedQuery.getQuery()));
			}

			@Override
			protected QuerySetting<NamedCodeCommentQuery> getQuerySetting() {
				return getProject().getCodeCommentQuerySettingOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedCodeCommentQuery> getProjectQueries() {
				return getProject().getSavedCodeCommentQueries();
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedCodeCommentQuery> querySetting) {
				getCodeCommentQuerySettingManager().save((CodeCommentQuerySetting) querySetting);
			}

			@Override
			protected void onSaveProjectQueries(ArrayList<NamedCodeCommentQuery> projectQueries) {
				getProject().setSavedCodeCommentQueries(projectQueries);
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
		
		add(new CodeCommentListPanel("main", new PropertyModel<String>(this, "query")) {

			@Override
			protected Project getProject() {
				return ProjectCodeCommentsPage.this.getProject();
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target) {
				PageParameters params = paramsOf(getProject(), query);
				setResponsePage(ProjectCodeCommentsPage.class, params);
			}

			@Override
			protected PullRequest getPullRequest() {
				return null;
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
										CodeCommentQuerySetting setting = getProject().getCodeCommentQuerySettingOfCurrentUser();
										NamedCodeCommentQuery namedQuery = setting.getUserQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedCodeCommentQuery(name, query);
											setting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getCodeCommentQuerySettingManager().save(setting);
										target.add(side);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										NamedCodeCommentQuery namedQuery = getProject().getSavedCodeCommentQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedCodeCommentQuery(name, query);
											getProject().getSavedCodeCommentQueries().add(namedQuery);
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
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}

package io.onedev.server.web.page.project.codecomments;

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
import io.onedev.server.entitymanager.CodeCommentQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.CodeCommentQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.codecomment.CodeCommentListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectCodeCommentsPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedCodeCommentQuery> savedQueries;
	
	private CodeCommentListPanel commentList;
	
	public ProjectCodeCommentsPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toString();
	}

	private CodeCommentQuerySettingManager getCodeCommentQuerySettingManager() {
		return OneDev.getInstance(CodeCommentQuerySettingManager.class);		
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(commentList);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<NamedCodeCommentQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedCodeCommentQuery> newNamedQueriesBean() {
				return new NamedCodeCommentQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedCodeCommentQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, ProjectCodeCommentsPage.class, 
						ProjectCodeCommentsPage.paramsOf(getProject(), namedQuery.getQuery(), 0));
			}

			@Override
			protected QuerySetting<NamedCodeCommentQuery> getQuerySetting() {
				return getProject().getCodeCommentQuerySettingOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedCodeCommentQuery> getQueries() {
				return getProject().getNamedCodeCommentQueries();
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedCodeCommentQuery> querySetting) {
				getCodeCommentQuerySettingManager().save((CodeCommentQuerySetting) querySetting);
			}

			@Override
			protected void onSaveQueries(ArrayList<NamedCodeCommentQuery> projectQueries) {
				getProject().setNamedCodeCommentQueries(projectQueries);
				OneDev.getInstance(ProjectManager.class).save(getProject());
			}

		});
		
		add(commentList = new CodeCommentListPanel("comments", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(ProjectCodeCommentsPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

			@Override
			protected Project getProject() {
				return ProjectCodeCommentsPage.this.getProject();
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int page) {
						return paramsOf(getProject(), query, page+1);
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
					public void onSaveQuery(AjaxRequestTarget target, @Nullable String query) {
						new ModalPanel(target)  {

							@Override
							protected Component newContent(String id) {
								return new SaveQueryPanel(id) {

									@Override
									protected void onSaveForMine(AjaxRequestTarget target, String name) {
										CodeCommentQuerySetting setting = getProject().getCodeCommentQuerySettingOfCurrentUser();
										NamedCodeCommentQuery namedQuery = NamedQuery.find(setting.getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedCodeCommentQuery(name, query);
											setting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getCodeCommentQuerySettingManager().save(setting);
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										NamedCodeCommentQuery namedQuery = NamedQuery.find(getProject().getNamedCodeCommentQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedCodeCommentQuery(name, query);
											getProject().getNamedCodeCommentQueries().add(namedQuery);
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
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
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
		if (project.getCodeCommentQuerySettingOfCurrentUser() != null 
				&& !project.getCodeCommentQuerySettingOfCurrentUser().getUserQueries().isEmpty()) {
			query = project.getCodeCommentQuerySettingOfCurrentUser().getUserQueries().iterator().next().getQuery();
		} else if (!project.getNamedCodeCommentQueries().isEmpty()) {
			query = project.getNamedCodeCommentQueries().iterator().next().getQuery();
		}
		return paramsOf(project, query, page);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Code Comments</span>").setEscapeModelStrings(false);
	}
	
	@Override
	protected String getPageTitle() {
		return "Code Comments - " + getProject().getName();
	}
	
}

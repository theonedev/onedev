package io.onedev.server.web.page.project.issues.list;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.util.NamedIssueQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectIssueListPage extends ProjectIssuesPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private SavedQueriesPanel<NamedIssueQuery> savedQueries;
	
	private IssueListPanel issueList;
	
	public ProjectIssueListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private IssueQueryPersonalizationManager getIssueQueryPersonalizationManager() {
		return OneDev.getInstance(IssueQueryPersonalizationManager.class);		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<NamedIssueQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedIssueQuery> newNamedQueriesBean() {
				return new NamedIssueQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedIssueQuery namedQuery) {
				PageParameters params = ProjectIssueListPage.paramsOf(
						getProject(), namedQuery.getQuery(), 0);
				return new BookmarkablePageLink<Void>(componentId, ProjectIssueListPage.class, params);
			}

			@Override
			protected QueryPersonalization<NamedIssueQuery> getQueryPersonalization() {
				return getProject().getIssueQueryPersonalizationOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedIssueQuery> getCommonQueries() {
				return (ArrayList<NamedIssueQuery>) getProject().getIssueSetting().getNamedQueries();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedIssueQuery> namedQueries) {
				getProject().getIssueSetting().setNamedQueries(namedQueries);
				OneDev.getInstance(ProjectManager.class).save(getProject());
			}

			@Override
			protected ArrayList<NamedIssueQuery> getInheritedCommonQueries() {
				if (getProject().getParent() != null)
					return (ArrayList<NamedIssueQuery>) getProject().getParent().getNamedIssueQueries();
				else
					return (ArrayList<NamedIssueQuery>) getIssueSetting().getNamedQueries();
			}
			
		});
		
		add(issueList = new IssueListPanel("issues", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(ProjectIssueListPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

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
								return new SaveQueryPanel(id, new PersonalQuerySupport() {

									@Override
									public void onSave(AjaxRequestTarget target, String name) {
										IssueQueryPersonalization setting = getProject().getIssueQueryPersonalizationOfCurrentUser();
										NamedIssueQuery namedQuery = NamedQuery.find(setting.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											setting.getQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getIssueQueryPersonalizationManager().save(setting);
										target.add(savedQueries);
										close();
									}
									
								}) {

									@Override
									protected void onSave(AjaxRequestTarget target, String name) {
										ProjectIssueSetting setting = getProject().getIssueSetting();
										if (setting.getNamedQueries() == null) 
											setting.setNamedQueries(new ArrayList<>(getIssueSetting().getNamedQueries()));
										NamedIssueQuery namedQuery = getProject().getNamedIssueQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											setting.getNamedQueries().add(namedQuery);
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
				return ProjectIssueListPage.this.getProject();
			}

		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(issueList);
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
		if (project.getIssueQueryPersonalizationOfCurrentUser() != null 
				&& !project.getIssueQueryPersonalizationOfCurrentUser().getQueries().isEmpty()) {
			query = project.getIssueQueryPersonalizationOfCurrentUser().getQueries().iterator().next().getQuery();
		} else if (!project.getNamedIssueQueries().isEmpty()) {
			query = project.getNamedIssueQueries().iterator().next().getQuery();
		}
		return paramsOf(project, query, page);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Issues");
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project, 0));
		else 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}

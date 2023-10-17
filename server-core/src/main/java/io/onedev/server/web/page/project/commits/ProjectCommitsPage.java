package io.onedev.server.web.page.project.commits;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CommitQueryPersonalizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.CommitQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.component.commit.status.CommitStatusSupport;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.QuerySaveSupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ProjectCommitsPage extends ProjectPage {

	private static final String PARAM_COMPARE = "compare";
	
	private static final String PARAM_QUERY = "query";
	
	private String compare;
	
	private String query;
	
	private SavedQueriesPanel<NamedCommitQuery> savedQueries;
	
	private CommitListPanel commitList;
	
	public ProjectCommitsPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toString();
		compare = params.get(PARAM_COMPARE).toString();
	}

	private CommitQueryPersonalizationManager getCommitQueryPersonalizationManager() {
		return OneDev.getInstance(CommitQueryPersonalizationManager.class);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(commitList);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(savedQueries = new SavedQueriesPanel<NamedCommitQuery>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedCommitQuery> newNamedQueriesBean() {
				return new NamedCommitQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedCommitQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, ProjectCommitsPage.class, 
						ProjectCommitsPage.paramsOf(getProject(), namedQuery.getQuery(), compare));
			}

			@Override
			protected QueryPersonalization<NamedCommitQuery> getQueryPersonalization() {
				return getProject().getCommitQueryPersonalizationOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedCommitQuery> getCommonQueries() {
				return getProject().getNamedCommitQueries();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedCommitQuery> projectQueries) {
				getProject().setNamedCommitQueries(projectQueries);
				OneDev.getInstance(ProjectManager.class).update(getProject());
			}

		});
		
		add(commitList = new CommitListPanel("commits", new IModel<String>() {

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
				CharSequence url = RequestCycle.get().urlFor(ProjectCommitsPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

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
										CommitQueryPersonalization setting = getProject().getCommitQueryPersonalizationOfCurrentUser();
										NamedCommitQuery namedQuery = NamedQuery.find(setting.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedCommitQuery(name, query);
											setting.getQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										if (setting.isNew())
											getCommitQueryPersonalizationManager().create(setting);
										else
											getCommitQueryPersonalizationManager().update(setting);
											
										target.add(savedQueries);
										close();
									}
									
								}) {

									@Override
									protected void onSave(AjaxRequestTarget target, String name) {
										NamedCommitQuery namedQuery = NamedQuery.find(getProject().getNamedCommitQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedCommitQuery(name, query);
											getProject().getNamedCommitQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(ProjectManager.class).update(getProject());
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
			protected CommitStatusSupport getStatusSupport() {
				return () -> null;
			}

			@Override
			protected String getCompareWith() {
				return compare;
			}

			@Override
			protected Project getProject() {
				return ProjectCommitsPage.this.getProject();
			}

		});
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query, @Nullable String compareWith) {
		PageParameters params = paramsOf(project);
		if (compareWith != null)
			params.set(PARAM_COMPARE, compareWith);
		if (query != null)
			params.set(PARAM_QUERY, query);
		return params;
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String compareWith) {
		String query = null;
		if (project.getCommitQueryPersonalizationOfCurrentUser() != null 
				&& !project.getCommitQueryPersonalizationOfCurrentUser().getQueries().isEmpty()) { 
			query = project.getCommitQueryPersonalizationOfCurrentUser().getQueries().iterator().next().getQuery();
		} else if (!project.getNamedCommitQueries().isEmpty()) {
			query = project.getNamedCommitQueries().iterator().next().getQuery();
		}
		return paramsOf(project, query, compareWith);
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Commits");
	}
	
	@Override
	protected String getPageTitle() {
		return "Commits - " + getProject().getPath();
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement() && SecurityUtils.canReadCode(project)) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(project));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
package io.onedev.server.web.page.project.commits;

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
import io.onedev.server.entitymanager.CommitQuerySettingManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.CommitQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.QuerySaveSupport;

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

	private CommitQuerySettingManager getCommitQuerySettingManager() {
		return OneDev.getInstance(CommitQuerySettingManager.class);
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
			protected QuerySetting<NamedCommitQuery> getQuerySetting() {
				return getProject().getCommitQuerySettingOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedCommitQuery> getQueries() {
				return getProject().getNamedCommitQueries();
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedCommitQuery> querySetting) {
				getCommitQuerySettingManager().save((CommitQuerySetting) querySetting);
			}

			@Override
			protected void onSaveQueries(ArrayList<NamedCommitQuery> projectQueries) {
				getProject().setNamedCommitQueries(projectQueries);
				OneDev.getInstance(ProjectManager.class).save(getProject());
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
								return new SaveQueryPanel(id) {

									@Override
									protected void onSaveForMine(AjaxRequestTarget target, String name) {
										CommitQuerySetting setting = getProject().getCommitQuerySettingOfCurrentUser();
										NamedCommitQuery namedQuery = NamedQuery.find(setting.getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedCommitQuery(name, query);
											setting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getCommitQuerySettingManager().save(setting);
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										NamedCommitQuery namedQuery = NamedQuery.find(getProject().getNamedCommitQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedCommitQuery(name, query);
											getProject().getNamedCommitQueries().add(namedQuery);
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
		if (project.getCommitQuerySettingOfCurrentUser() != null 
				&& !project.getCommitQuerySettingOfCurrentUser().getUserQueries().isEmpty()) { 
			query = project.getCommitQuerySettingOfCurrentUser().getUserQueries().iterator().next().getQuery();
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
		return "Commits - " + getProject().getName();
	}
	
}
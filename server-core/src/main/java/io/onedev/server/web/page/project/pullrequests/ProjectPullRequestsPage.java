package io.onedev.server.web.page.project.pullrequests;

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
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestQuerySettingManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestQuerySetting;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.administration.GlobalPullRequestSetting;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.pullrequest.list.PullRequestListPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.NamedPullRequestQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectPullRequestsPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;			
	
	private SavedQueriesPanel<NamedPullRequestQuery> savedQueries;
	
	private PullRequestListPanel requestList;
	
	public ProjectPullRequestsPage(PageParameters params) {
		super(params);
		query = getPageParameters().get(PARAM_QUERY).toOptionalString();
	}

	private PullRequestQuerySettingManager getPullRequestQuerySettingManager() {
		return OneDev.getInstance(PullRequestQuerySettingManager.class);		
	}
	
	protected GlobalPullRequestSetting getPullRequestSetting() {
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
				return new BookmarkablePageLink<Void>(componentId, ProjectPullRequestsPage.class, 
						ProjectPullRequestsPage.paramsOf(getProject(), namedQuery.getQuery(), 0));
			}

			@Override
			protected QuerySetting<NamedPullRequestQuery> getQuerySetting() {
				return getProject().getPullRequestQuerySettingOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedPullRequestQuery> getQueries() {
				return (ArrayList<NamedPullRequestQuery>) getProject().getPullRequestSetting().getNamedQueries(false);
			}

			@Override
			protected ArrayList<NamedPullRequestQuery> getDefaultQueries() {
				return (ArrayList<NamedPullRequestQuery>) getPullRequestSetting().getNamedQueries();
			}
			
			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedPullRequestQuery> querySetting) {
				getPullRequestQuerySettingManager().save((PullRequestQuerySetting) querySetting);
			}

			@Override
			protected void onSaveQueries(ArrayList<NamedPullRequestQuery> namedQueries) {
				getProject().getPullRequestSetting().setNamedQueries(namedQueries);
				OneDev.getInstance(ProjectManager.class).save(getProject());
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
				CharSequence url = RequestCycle.get().urlFor(ProjectPullRequestsPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						PageParameters params = paramsOf(getProject(), query, 0);
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
										PullRequestQuerySetting setting = getProject().getPullRequestQuerySettingOfCurrentUser();
										NamedPullRequestQuery namedQuery = NamedQuery.find(setting.getUserQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedPullRequestQuery(name, query);
											setting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getPullRequestQuerySettingManager().save(setting);
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onSaveForAll(AjaxRequestTarget target, String name) {
										ProjectPullRequestSetting setting = getProject().getPullRequestSetting();
										if (setting.getNamedQueries(false) == null) 
											setting.setNamedQueries(new ArrayList<>(getPullRequestSetting().getNamedQueries()));
										NamedPullRequestQuery namedQuery = setting.getNamedQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedPullRequestQuery(name, query);
											setting.getNamedQueries(false).add(namedQuery);
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
				return ProjectPullRequestsPage.this.getProject();
			}
			
		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(requestList);
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
		if (project.getPullRequestQuerySettingOfCurrentUser() != null
				&& !project.getPullRequestQuerySettingOfCurrentUser().getUserQueries().isEmpty()) {
			query = project.getPullRequestQuerySettingOfCurrentUser().getUserQueries().iterator().next().getQuery();
		} else if (!project.getPullRequestSetting().getNamedQueries(true).isEmpty()) {
			query = project.getPullRequestSetting().getNamedQueries(true).iterator().next().getQuery();
		}
		return paramsOf(project, query, page);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-nowrap'>Pull Requests</span>").setEscapeModelStrings(false);
	}

	@Override
	protected String getPageTitle() {
		return "Pull Requests - " + getProject().getName();
	}
	
}

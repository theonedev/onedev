package io.onedev.server.web.page.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.facade.GroupAuthorizationFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserAuthorizationFacade;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.project.list.ProjectListPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.utils.matchscore.MatchScoreProvider;
import io.onedev.utils.matchscore.MatchScoreUtils;

@SuppressWarnings("serial")
public class ProjectListPage extends LayoutPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_ORPHAN = "orphan";
	
	private boolean showOrphanProjects;
	
	private String query;
	
	private final IModel<List<ProjectFacade>> orphanProjectsModel = new LoadableDetachableModel<List<ProjectFacade>>() {

		@Override
		protected List<ProjectFacade> load() {
			CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
			List<ProjectFacade> projects = new ArrayList<>();
			
			Set<Long> projectIdsWithExplicitAdministrators = new HashSet<>();
			for (UserAuthorizationFacade authorization: cacheManager.getUserAuthorizations().values()) {
				if (authorization.getPrivilege() == ProjectPrivilege.ADMINISTRATION)
					projectIdsWithExplicitAdministrators.add(authorization.getProjectId());
			}
			for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
				if (authorization.getPrivilege() == ProjectPrivilege.ADMINISTRATION)
					projectIdsWithExplicitAdministrators.add(authorization.getProjectId());
			}
			
			for (ProjectFacade project: cacheManager.getProjects().values()) {
				if (!projectIdsWithExplicitAdministrators.contains(project.getId())) {
					projects.add(project);
				}
			}
			projects.sort(Comparator.comparing(ProjectFacade::getName));
			
			return MatchScoreUtils.filterAndSort(projects, new MatchScoreProvider<ProjectFacade>() {

				@Override
				public double getMatchScore(ProjectFacade object) {
					return MatchScoreUtils.getMatchScore(object.getName(), query);
				}
				
			});
		}
		
	};
	
	public ProjectListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
		showOrphanProjects = params.get(PARAM_ORPHAN).toBoolean(false);
	}
	
	private final IModel<List<ProjectFacade>> projectsModel = new LoadableDetachableModel<List<ProjectFacade>>() {

		@Override
		protected List<ProjectFacade> load() {
			List<ProjectFacade> projects = new ArrayList<>(OneDev.getInstance(ProjectManager.class)
					.getAccessibleProjects(getLoginUser()));
			projects.sort(ProjectFacade::compareLastVisit);
			return MatchScoreUtils.filterAndSort(projects, new MatchScoreProvider<ProjectFacade>() {

				@Override
				public double getMatchScore(ProjectFacade object) {
					return MatchScoreUtils.getMatchScore(object.getName(), query);
				}
				
			});
		}
		
	};
	
	private ProjectListPanel projectList;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("filterProjects", Model.of(query)));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				query = searchField.getInput();
				if (StringUtils.isBlank(query))
					query = null;
				target.add(projectList);
			}

		});
		add(new BookmarkablePageLink<Void>("createProject", NewProjectPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canCreateProjects());
			}
			
		});

		WebMarkupContainer orphanProjectsNote = new WebMarkupContainer("orphanProjectsNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(showOrphanProjects);
			}
			
		};
		orphanProjectsNote.setOutputMarkupPlaceholderTag(true);
		add(orphanProjectsNote);
		
		add(new Link<Void>("showOrphanProjects") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return showOrphanProjects?"active":"";
					}
					
				}));
				setOutputMarkupPlaceholderTag(true);				
			}

			@Override
			public void onClick() {
				showOrphanProjects = !showOrphanProjects;
				PageParameters params = new PageParameters();
				params.add(PARAM_ORPHAN, showOrphanProjects);
				setResponsePage(ProjectListPage.class, params);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator() && !orphanProjectsModel.getObject().isEmpty());
			}
			
		});
		
		add(projectList = new ProjectListPanel("projects", new AbstractReadOnlyModel<List<ProjectFacade>>() {

			@Override
			public List<ProjectFacade> getObject() {
				if (showOrphanProjects)
					return orphanProjectsModel.getObject();
				else
					return projectsModel.getObject();
			}
			
		}, new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = new PageParameters();
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				if (query != null)
					params.add(PARAM_QUERY, query);
				if (showOrphanProjects)
					params.add(PARAM_ORPHAN, showOrphanProjects);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		}));
	}

	@Override
	protected void onDetach() {
		orphanProjectsModel.detach();
		projectsModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectResourceReference()));
	}

}

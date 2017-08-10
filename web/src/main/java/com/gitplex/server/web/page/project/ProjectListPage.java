package com.gitplex.server.web.page.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.projectlist.ProjectListPanel;
import com.gitplex.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class ProjectListPage extends LayoutPage {

	private boolean showOrphanProjects;
	
	private final IModel<List<ProjectFacade>> orphanProjectsModel = new LoadableDetachableModel<List<ProjectFacade>>() {

		@Override
		protected List<ProjectFacade> load() {
			CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
			List<ProjectFacade> projects = new ArrayList<>();
			
			Set<Long> projectIdsWithExplicitAdministrators = new HashSet<>();
			for (UserAuthorizationFacade authorization: cacheManager.getUserAuthorizations().values()) {
				if (authorization.getPrivilege() == ProjectPrivilege.ADMIN)
					projectIdsWithExplicitAdministrators.add(authorization.getProjectId());
			}
			for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
				if (authorization.getPrivilege() == ProjectPrivilege.ADMIN)
					projectIdsWithExplicitAdministrators.add(authorization.getProjectId());
			}
			
			for (ProjectFacade project: cacheManager.getProjects().values()) {
				if (!projectIdsWithExplicitAdministrators.contains(project.getId()) 
						&& project.matchesQuery(searchInput)) {
					projects.add(project);
				}
			}
			projects.sort(Comparator.comparing(ProjectFacade::getName));
			return projects;
		}
		
	};
	
	private final IModel<List<ProjectFacade>> projectsModel = new LoadableDetachableModel<List<ProjectFacade>>() {

		@Override
		protected List<ProjectFacade> load() {
			List<ProjectFacade> projects = new ArrayList<>(GitPlex.getInstance(ProjectManager.class)
					.getAccessibleProjects(getLoginUser()));
			for (Iterator<ProjectFacade> it = projects.iterator(); it.hasNext();) {
				if (!it.next().matchesQuery(searchInput))
					it.remove();
			}
			projects.sort(Comparator.comparing(ProjectFacade::getName));
			return projects;
		}
		
	};
	
	private ProjectListPanel projectList;
	
	private String searchInput;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("filterProjects", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
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
		
		add(new AjaxLink<Void>("showOrphanProjects") {

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
			public void onClick(AjaxRequestTarget target) {
				showOrphanProjects = !showOrphanProjects;
				target.add(this);
				target.add(projectList);
				target.add(orphanProjectsNote);
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

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();
		
		breadcrumbs.add(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, ProjectListPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Projects");
					}
					
				};
			}
			
		});

		return breadcrumbs;
	}

}

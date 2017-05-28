package com.gitplex.server.web.page.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.page.layout.NewProjectPage;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {

	private PageableListView<Project> projectsView;
	
	private BootstrapPagingNavigator projectsPageNav;
	
	private WebMarkupContainer projectsContainer; 
	
	private WebMarkupContainer noProjectsContainer;
	
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
				target.add(projectsContainer);
				target.add(projectsPageNav);
				target.add(noProjectsContainer);
			}

		});
		add(new BookmarkablePageLink<Void>("createProject", NewProjectPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canCreateProjects());
			}
			
		});
		
		projectsContainer = new WebMarkupContainer("projects") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!projectsView.getModelObject().isEmpty());
			}
			
		};
		projectsContainer.setOutputMarkupPlaceholderTag(true);
		add(projectsContainer);
		
		projectsContainer.add(projectsView = new PageableListView<Project>("projects", 
				new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				ProjectManager projectManager = GitPlex.getInstance(ProjectManager.class);
				List<Project> projects = new ArrayList<>();
				for (Project project: projectManager.findAllAccessible(getLoginUser())) {
					if (project.matches(searchInput)) {
						projects.add(project);
					}
				}
				projects.sort(Project::compareLastVisit);
				return projects;
			}
			
		}, WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Project> item) {
				Project project = item.getModelObject();
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
						ProjectBlobPage.class, ProjectBlobPage.paramsOf(project)); 
				link.add(new Label("name", project.getName()));
				item.add(link);
			}
			
		});

		add(projectsPageNav = new BootstrapAjaxPagingNavigator("projectsPageNav", projectsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(projectsView.getPageCount() > 1);
			}
			
		});
		projectsPageNav.setOutputMarkupPlaceholderTag(true);
		
		add(noProjectsContainer = new WebMarkupContainer("noProjects") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(projectsView.getModelObject().isEmpty());
			}
			
		});
		noProjectsContainer.setOutputMarkupPlaceholderTag(true);		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DashboardResourceReference()));
	}

	@Override
	protected Component newContextHead(String componentId) {
		return new Label(componentId, "Projects");
	}

}

package io.onedev.server.web.page.project;

import static io.onedev.server.ai.ToolUtils.wrapForChat;
import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.ai.ChatTool;
import io.onedev.server.ai.ChatToolAware;
import io.onedev.server.ai.tools.issue.CreateIssue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.timetracking.TimeTrackingService;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.dropdowntriangleindicator.DropdownTriangleIndicatorCssResourceReference;
import io.onedev.server.web.avatar.AvatarService;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.component.RepeatingView;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.ProjectAvatar;
import io.onedev.server.web.component.project.childrentree.ProjectChildrenTree;
import io.onedev.server.web.mapper.ProjectMapperUtils;
import io.onedev.server.web.opengraph.OpenGraphHeaderMeta;
import io.onedev.server.web.opengraph.OpenGraphHeaderMetaType;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.layout.SidebarMenu;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.InvalidBuildPage;
import io.onedev.server.web.page.project.codecomments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.issues.boards.IssueBoardsPage;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.iteration.IterationDetailPage;
import io.onedev.server.web.page.project.issues.iteration.IterationEditPage;
import io.onedev.server.web.page.project.issues.iteration.IterationListPage;
import io.onedev.server.web.page.project.issues.iteration.NewIterationPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.overview.ProjectOverviewPage;
import io.onedev.server.web.page.project.packs.ProjectPacksPage;
import io.onedev.server.web.page.project.packs.detail.PackDetailPage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.setting.ProjectSettingMenu;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.page.project.stats.code.CodeContribsPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.page.project.wiki.ProjectWikiPage;
import io.onedev.server.web.page.project.workspaces.ProjectWorkspacesPage;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDetailPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.util.ProjectAware;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

public abstract class ProjectPage extends LayoutPage implements ProjectAware, ChatToolAware {

	protected final IModel<Project> projectModel;

	@Inject
	protected ProjectService projectService;
	
	public ProjectPage(PageParameters params) {
		super(params);
	
		String projectPath = params.get(ProjectMapperUtils.PARAM_PROJECT).toOptionalString();
		if (projectPath == null)
			throw new RestartResponseException(ProjectListPage.class);
		
		projectPath = StringUtils.strip(projectPath, "/");
		
		Project project;
		if (Project.DEFAULT_NAME.equals(projectPath)) {
			if (!(this instanceof ProjectSettingPage))
				throw new IllegalStateException();
			if (!SecurityUtils.isAdministrator())
				throw new UnauthorizedException();
			project = getProjectService().load(Project.DEFAULT_ID);
		} else {
			project = getProjectService().findByPath(projectPath);
		}
		if (project == null || !SecurityUtils.canAccessProject(project)) {
			if (getLoginUser() != null)
				throw new EntityNotFoundException("Project not found or inaccessible");
			else
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
		}

		Long projectId = project.getId();
		projectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				Project project = OneDev.getInstance(ProjectService.class).load(projectId);
				
				/*
				 * Give child page a chance to cache object id of known revisions upon
				 * loading the project object 
				 */
				for (Map.Entry<String, ObjectId> entry: getObjectIdCache().entrySet()) {
					project.cacheObjectId(entry.getKey(), entry.getValue());
				}
				return project;
			}
			
		};
		projectModel.setObject(project);
		
		if (!(this instanceof ProjectSettingPage) 
				&& !(this instanceof ProjectOverviewPage)
				&& !(this instanceof NoProjectStoragePage) 
				&& getProject().getActiveServer(false) == null) {
			throw new RestartResponseException(NoProjectStoragePage.class, 
					NoProjectStoragePage.paramsOf(getProject()));
		}
	}
	
	protected Map<String, ObjectId> getObjectIdCache() {
		return new HashMap<>();
	}
		
	@Override
	public Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	protected List<SidebarMenu> getSidebarMenus() {
		if (Project.DEFAULT_ID.equals(getProject().getId()))
			return super.getSidebarMenus();
		List<SidebarMenuItem> menuItems = new ArrayList<>();

		menuItems.add(new SidebarMenuItem.Page("dashboard", _T("Overview"),
				ProjectOverviewPage.class, ProjectOverviewPage.paramsOf(getProject())));
		
		if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject())) {
			List<SidebarMenuItem> codeMenuItems = new ArrayList<>();
	
			codeMenuItems.add(new SidebarMenuItem.Page(null, _T("Files"), 
					ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject())));
			codeMenuItems.add(new SidebarMenuItem.Page(null, _T("Commits"), 
					ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(getProject(), null), 
					Lists.newArrayList(CommitDetailPage.class)));
			codeMenuItems.add(new SidebarMenuItem.Page(null, _T("Branches"), 
					ProjectBranchesPage.class, ProjectBranchesPage.paramsOf(getProject())));
			codeMenuItems.add(new SidebarMenuItem.Page(null, _T("Tags"), 
					ProjectTagsPage.class, ProjectTagsPage.paramsOf(getProject())));
			codeMenuItems.add(new SidebarMenuItem.Page(null, _T("Code Comments"), 
					ProjectCodeCommentsPage.class, ProjectCodeCommentsPage.paramsOf(getProject(), 0)));
			codeMenuItems.add(new SidebarMenuItem.Page(null, _T("Code Compare"), 
					RevisionComparePage.class, RevisionComparePage.paramsOf(getProject())));
			
			menuItems.add(new SidebarMenuItem.SubMenu("git", _T("Code"), codeMenuItems));
		}
		if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject())) {
			menuItems.add(new SidebarMenuItem.Page("pull-request", _T("Pull Requests"),
					ProjectPullRequestsPage.class, ProjectPullRequestsPage.paramsOf(getProject(), 0),
					Lists.newArrayList(NewPullRequestPage.class, PullRequestDetailPage.class, InvalidPullRequestPage.class)));
		}
		if (getProject().isIssueManagement()) {
			List<SidebarMenuItem> issueMenuItems = new ArrayList<>();
			
			issueMenuItems.add(new SidebarMenuItem.Page(null, _T("List"), 
					ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(getProject(), 0), 
					Lists.newArrayList(NewIssuePage.class, IssueDetailPage.class)));
			issueMenuItems.add(new SidebarMenuItem.Page(null, _T("Boards"), 
					IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject())));
			issueMenuItems.add(new SidebarMenuItem.Page(null, _T("Iterations"), 
					IterationListPage.class, IterationListPage.paramsOf(getProject(), false, null), 
					Lists.newArrayList(NewIterationPage.class, IterationDetailPage.class, IterationEditPage.class)));
			if (getProject().isTimeTracking() && isSubscriptionActive() && SecurityUtils.canAccessTimeTracking(getProject())) 
				issueMenuItems.add(OneDev.getInstance(TimeTrackingService.class).newTimesheetsMenuItem(getProject()));
			menuItems.add(new SidebarMenuItem.SubMenu("issue", _T("Issues"), issueMenuItems));
		}

		if (getProject().isCodeManagement()) {
			menuItems.add(new SidebarMenuItem.Page("play-circle", _T("Builds"),
					ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(getProject(), 0),
					Lists.newArrayList(BuildDetailPage.class, InvalidBuildPage.class)));
		}
		
		if (getProject().isPackManagement() && SecurityUtils.canReadPack(getProject())) {
			menuItems.add(new SidebarMenuItem.Page("package", _T("Packages"),
					ProjectPacksPage.class, ProjectPacksPage.paramsOf(getProject(), 0),
					Lists.newArrayList(PackDetailPage.class)));
		}

		if (getProject().isCodeManagement() && SecurityUtils.canCreateWorkspaces(getProject()) 
				&& !getProject().getHierarchyWorkspaceSpecs().isEmpty()) {
			menuItems.add(new SidebarMenuItem.Page("workspace", _T("Workspaces"),
					ProjectWorkspacesPage.class, ProjectWorkspacesPage.paramsOf(getProject(), 0),
					Lists.newArrayList(WorkspaceDetailPage.class)));
		}

		if (getProject().isCodeManagement() && getProject().isWikiManagement()
				&& SecurityUtils.canAccessProject(getProject())) {
			menuItems.add(new SidebarMenuItem.Page("wiki", _T("Wiki"),
					ProjectWikiPage.class, ProjectWikiPage.paramsOf(getProject())));
		}

		List<SidebarMenuItem> statsMenuItems = new ArrayList<>();
		
		if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject())) {
			statsMenuItems.add(new SidebarMenuItem.Page(null, _T("Code Contributions"), 
					CodeContribsPage.class, CodeContribsPage.paramsOf(getProject())));
		}

		// Add the sub menu even if it is empty as we need to place stats menu in the right place.
		// Menu items may be added to the sub menu later via contribution
		menuItems.add(new SidebarMenuItem.SubMenu("stats", _T("Statistics"), statsMenuItems));
		
		if (SecurityUtils.canManageProject(getProject())) {
			menuItems.add(new SidebarMenuItem.SubMenu("sliders", _T("Settings"),
					ProjectSettingMenu.getMenuItems(getProject())));
		}

		String avatarUrl = OneDev.getInstance(AvatarService.class).getProjectAvatarUrl(getProject().getId());
		var menu = new SidebarMenu(new SidebarMenu.Header(avatarUrl, getProject().getName()), menuItems);

		var contributions = new ArrayList<>(OneDev.getExtensions(ProjectMenuContribution.class));
		contributions.sort(Comparator.comparing(ProjectMenuContribution::getOrder));
		
		for (ProjectMenuContribution contribution: contributions) {
			for (var menuItem: contribution.getMenuItems(getProject())) 
				menu.insertMenuItem(menuItem);			
		}
		
		List<SidebarMenu> menus = super.getSidebarMenus();	
		menus.add(menu);
		return menus;
	}

	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}
	
	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("projects", ProjectListPage.class, ProjectListPage.paramsOf(0, 0)));
		fragment.add(new DropdownLink("favorites") {

			private Component newItem(String componentId, Project project) {
				WebMarkupContainer item = new WebMarkupContainer(componentId);
				WebMarkupContainer link = navToProject("link", project);
				link.add(new ProjectAvatar("avatar", project.getId()));
				link.add(new Label("label", project.getPath()));
				item.add(link);
				return item;
			}
			
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "favoritesFrag", ProjectPage.this);
				RepeatingView projectsView = new RepeatingView("projects");
				
				String queryString = getProjectService().getFavoriteQuery(getLoginUser());
				ProjectQuery query = ProjectQuery.parse(queryString);
				for (Project project: getProjectService().query(SecurityUtils.getSubject(), query, false, 0, WebConstants.PAGE_SIZE)) 
					projectsView.add(newItem(projectsView.newChildId(), project));
				
				fragment.add(projectsView);
				
				fragment.add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {
					
					@Override
					protected void appendMore(AjaxRequestTarget target, int offset, int count) {
						for (Project project: getProjectService().query(SecurityUtils.getSubject(), query, false, offset, count)) {
							Component item = newItem(projectsView.newChildId(), project);
							projectsView.add(item);
							String script = String.format("$('#%s ul').append('<li id=\"%s\"></li>');", 
									fragment.getMarkupId(), item.getMarkupId());
							target.prependJavaScript(script);
							target.add(item);
						}
					}

					@Override
					protected String getItemSelector() {
						return "li";
					}
					
				});
				
				fragment.add(AttributeAppender.append("class", "autosuit"));
				
				return fragment;
			}
			
		});

		fragment.add(new ListView<Project>("pathSegments", new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				List<Project> projects = new ArrayList<>();
				Project project = getProject();
				do {
					projects.add(project);
					project = project.getParent();
				} while (project != null);
				
				Collections.reverse(projects);
				return projects;
			}
			
		}) {
			
			@Override
			protected void populateItem(ListItem<Project> item) {
				Project project = item.getModelObject();
				if (SecurityUtils.canAccessProject(project)) {
					WebMarkupContainer link = navToProject("link", project);
					link.add(new Label("label", project.getName()));
					item.add(link);
					
					if (item.getIndex() < getModelObject().size() - 1 || getProjectService().hasChildren(project.getId())) {
						Long projectId = project.getId();
						item.add(new DropdownLink("children") {
	
							@Override
							protected Component newContent(String id, FloatingPanel dropdown) {
								return new ProjectChildrenTree(id, projectId) {

									@Override
									protected WebMarkupContainer newChildLink(String componentId, Long childId) {
										return navToProject(componentId, ProjectPage.getProjectService().load(childId));
									}
									
								};
							}
							
						});
						item.add(new WebMarkupContainer("dot").setVisible(false));
					} else {
						item.add(new WebMarkupContainer("children").setVisible(false));
						item.add(new Label("dot").add(AttributeAppender.append("class", "dot")));
					}
				} else {
					WebMarkupContainer link = new WebMarkupContainer("link") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("span");
						}
						
					};
					link.add(new Label("label", project.getName()));
					item.add(link);
					item.add(new WebMarkupContainer("children").setVisible(false));
					item.add(new Label("dot").add(AttributeAppender.append("class", "dot")));
				}
			}
			
		});

		WebMarkupContainer compactProjectPath = navToProject("compactProjectPath", getProject());
		compactProjectPath.add(new Label("label", getProject().getPath()));
		fragment.add(compactProjectPath);

		fragment.add(newProjectTitle("projectTitle"));
		return fragment;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		if (Project.DEFAULT_ID.equals(getProject().getId()))
			return;

		String description = getProject().getDescription();
		if(description == null || description.equals("")) {
			description = getProject().getName();
		}
		
		String urlOfProjectImage = getSettingService().getSystemSetting().getServerUrl() +
				OneDev.getInstance(AvatarService.class).getProjectAvatarUrl(getProject().getId());
		
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Title, getProject().getPath()).render(response);
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Description, description).render(response);
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Image, 
				urlOfProjectImage).render(response);
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Url, getProject().getUrl()).render(response);

		response.render(CssHeaderItem.forReference(new ProjectCssResourceReference()));
		response.render(CssHeaderItem.forReference(new DropdownTriangleIndicatorCssResourceReference()));
		response.render(JavaScriptHeaderItem.forReference(new ProjectResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return getProject().getPath();
	}

	protected abstract BookmarkablePageLink<Void> navToProject(String componentId, Project project);
	
	protected static ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
	
	protected abstract Component newProjectTitle(String componentId);

	@Override
	public List<ChatTool> getChatTools() {
		if (Project.DEFAULT_ID.equals(getProject().getId()))
			return Collections.emptyList();
		var tools = new ArrayList<ChatTool>();
		if (getProject().isIssueManagement()) 
			tools.add(wrapForChat(new CreateIssue(getProject().getId())));
		return tools;
	}

	public static PageParameters paramsOf(Long projectId) {
		if (Project.DEFAULT_ID.equals(projectId))
			return paramsOf(Project.DEFAULT_NAME);
		ProjectFacade project = getProjectService().findFacadeById(projectId);
		return paramsOf(project.getPath());
	}
	
	public static PageParameters paramsOf(String projectPath) {
		PageParameters params = new PageParameters();
		params.add(ProjectMapperUtils.PARAM_PROJECT, projectPath);
		return params;
	}
	
	public static PageParameters paramsOf(Project project) {
		return Project.DEFAULT_ID.equals(project.getId()) ? paramsOf(Project.DEFAULT_NAME) : paramsOf(project.getPath());
	}
	
}

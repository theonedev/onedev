package io.onedev.server.web.page.project;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.timetracking.TimeTrackingManager;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.dropdowntriangleindicator.DropdownTriangleIndicatorCssResourceReference;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.ProjectAvatar;
import io.onedev.server.web.component.project.childrentree.ProjectChildrenTree;
import io.onedev.server.web.component.project.info.ProjectInfoPanel;
import io.onedev.server.web.editable.EditableUtils;
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
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
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
import io.onedev.server.web.page.project.packs.ProjectPacksPage;
import io.onedev.server.web.page.project.packs.detail.PackDetailPage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.page.project.setting.authorization.GroupAuthorizationsPage;
import io.onedev.server.web.page.project.setting.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.project.setting.avatar.AvatarEditPage;
import io.onedev.server.web.page.project.setting.build.BuildPreservationsPage;
import io.onedev.server.web.page.project.setting.build.CacheManagementPage;
import io.onedev.server.web.page.project.setting.build.DefaultFixedIssueFiltersPage;
import io.onedev.server.web.page.project.setting.build.JobPropertiesPage;
import io.onedev.server.web.page.project.setting.build.JobSecretsPage;
import io.onedev.server.web.page.project.setting.code.analysis.CodeAnalysisSettingPage;
import io.onedev.server.web.page.project.setting.code.branchprotection.BranchProtectionsPage;
import io.onedev.server.web.page.project.setting.code.git.GitPackConfigPage;
import io.onedev.server.web.page.project.setting.code.pullrequest.PullRequestSettingPage;
import io.onedev.server.web.page.project.setting.code.tagprotection.TagProtectionsPage;
import io.onedev.server.web.page.project.setting.general.GeneralProjectSettingPage;
import io.onedev.server.web.page.project.setting.pluginsettings.ContributedProjectSettingPage;
import io.onedev.server.web.page.project.setting.servicedesk.ServiceDeskSettingPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.stats.code.CodeContribsPage;
import io.onedev.server.web.page.project.stats.code.SourceLinesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.page.simple.security.LoginPage;
import io.onedev.server.web.util.ProjectAware;

public abstract class ProjectPage extends LayoutPage implements ProjectAware {

	protected final IModel<Project> projectModel;
	
	public ProjectPage(PageParameters params) {
		super(params);

		Request request = RequestCycle.get().getRequest();
		String requestUrl = request.getUrl().toString();
		requestUrl = StringUtils.stripStart(requestUrl, "/");
		if (requestUrl.startsWith("projects/")) {
			requestUrl = StringUtils.stripStart(requestUrl.substring("projects/".length()), "/");
			Long projectId = Long.valueOf(StringUtils.substringBefore(requestUrl, "/"));
			Project project = getProjectManager().load(projectId);
			String suffix = StringUtils.substringAfter(requestUrl, "/");
			
			String redirectUrl = "/" + project.getPath();
			if (StringUtils.isNotBlank(suffix)) 
				redirectUrl += "/~" + suffix;
			
			throw new RedirectToUrlException(redirectUrl, HttpServletResponse.SC_MOVED_PERMANENTLY);
		}
	
		String projectPath = params.get(ProjectMapperUtils.PARAM_PROJECT).toOptionalString();
		if (projectPath == null)
			throw new RestartResponseException(ProjectListPage.class);
		
		projectPath = StringUtils.strip(projectPath, "/");
		
		Project project = getProjectManager().findByPath(projectPath);
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
				Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
				
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
				&& !(this instanceof ProjectChildrenPage)
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
	protected void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	public Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	protected List<SidebarMenu> getSidebarMenus() {
		List<SidebarMenuItem> menuItems = new ArrayList<>();
		
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
				issueMenuItems.add(OneDev.getInstance(TimeTrackingManager.class).newTimesheetsMenuItem(getProject()));
			menuItems.add(new SidebarMenuItem.SubMenu("bug", _T("Issues"), issueMenuItems));
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
		
		List<SidebarMenuItem> statsMenuItems = new ArrayList<>();
		
		if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject())) {
			statsMenuItems.add(new SidebarMenuItem.Page(null, _T("Code"), 
					CodeContribsPage.class, CodeContribsPage.paramsOf(getProject()), 
					Lists.newArrayList(SourceLinesPage.class)));
		}

		// Add the sub menu even if it is empty as we need to place stats menu in the right place.
		// Menu items may be added to the sub menu later via contribution
		menuItems.add(new SidebarMenuItem.SubMenu("stats", _T("Statistics"), statsMenuItems));
		
		menuItems.add(new SidebarMenuItem.Page("tree", _T("Child Projects"), 
				ProjectChildrenPage.class, ProjectChildrenPage.paramsOf(getProject(), null, 0)));
		
		if (SecurityUtils.canManageProject(getProject())) {
			List<SidebarMenuItem> settingMenuItems = new ArrayList<>();
			settingMenuItems.add(new SidebarMenuItem.Page(null, _T("General"), 
					GeneralProjectSettingPage.class, GeneralProjectSettingPage.paramsOf(getProject())));
			settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Edit Avatar"), 
					AvatarEditPage.class, AvatarEditPage.paramsOf(getProject())));

			List<SidebarMenuItem> authorizationMenuItems = new ArrayList<>();
			authorizationMenuItems.add(new SidebarMenuItem.Page(null, _T("By User"), 
					UserAuthorizationsPage.class, UserAuthorizationsPage.paramsOf(getProject())));
			authorizationMenuItems.add(new SidebarMenuItem.Page(null, _T("By Group"),
					GroupAuthorizationsPage.class, GroupAuthorizationsPage.paramsOf(getProject())));
			settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Authorization"), authorizationMenuItems));

			List<SidebarMenuItem> codeSettingMenuItems = new ArrayList<>();
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Branch Protection"), 
					BranchProtectionsPage.class, BranchProtectionsPage.paramsOf(getProject())));
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Tag Protection"), 
					TagProtectionsPage.class, TagProtectionsPage.paramsOf(getProject())));
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Code Analysis"), 
					CodeAnalysisSettingPage.class, CodeAnalysisSettingPage.paramsOf(getProject())));
			if (getProject().isCodeManagement()) {
				codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Git Pack Config"),
						GitPackConfigPage.class, GitPackConfigPage.paramsOf(getProject())));
			}
			
			settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Code"), codeSettingMenuItems));
			settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Pull Request"),
					PullRequestSettingPage.class, PullRequestSettingPage.paramsOf(getProject())));
			
			List<SidebarMenuItem> buildSettingMenuItems = new ArrayList<>();
			
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Job Secrets"), 
					JobSecretsPage.class, JobSecretsPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Job Properties"),
					JobPropertiesPage.class, JobPropertiesPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Build Preserve Rules"), 
					BuildPreservationsPage.class, BuildPreservationsPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Default Fixed Issue Filters"), 
					DefaultFixedIssueFiltersPage.class, DefaultFixedIssueFiltersPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Cache Management"),
					CacheManagementPage.class, CacheManagementPage.paramsOf(getProject())));
			
			settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Build"), buildSettingMenuItems));
			
			if (getSettingManager().getServiceDeskSetting() != null && getProject().isIssueManagement()) {
				settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Service Desk"), 
						ServiceDeskSettingPage.class, ServiceDeskSettingPage.paramsOf(getProject())));
			}
			
			SidebarMenuItem webHooksItem = new SidebarMenuItem.Page(null, _T("Web Hooks"), 
					WebHooksPage.class, WebHooksPage.paramsOf(getProject()));			
			settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Notification"), Lists.newArrayList(webHooksItem)));	

			menuItems.add(new SidebarMenuItem.SubMenu("sliders", _T("Settings"), settingMenuItems));
		}
		
		String avatarUrl = OneDev.getInstance(AvatarManager.class).getProjectAvatarUrl(getProject().getId());
		SidebarMenu.Header menuHeader = new SidebarMenu.Header(avatarUrl, getProject().getName()) {
			
			@Override
			protected Component newMoreInfo(String componentId, FloatingPanel dropdown) {
				return new ProjectInfoPanel(componentId, projectModel) {

					@Override
					protected void onPromptForkOption(AjaxRequestTarget target) {
						dropdown.close();
					}
					
				};
			}
			
		};
		var menu = new SidebarMenu(menuHeader, menuItems);
		
		if (SecurityUtils.canManageProject(getProject())) {
			List<Class<? extends ContributedProjectSetting>> contributedSettingClasses = new ArrayList<>();
			for (ProjectSettingContribution contribution:OneDev.getExtensions(ProjectSettingContribution.class)) {
				for (Class<? extends ContributedProjectSetting> settingClass: contribution.getSettingClasses()) 
					contributedSettingClasses.add(settingClass);
			}
			contributedSettingClasses.sort(Comparator.comparingInt(EditableUtils::getOrder));
						
			for (var contributedSettingClass: contributedSettingClasses) {
				var menuItem = new SidebarMenuItem.Page(
						null,
						_T(EditableUtils.getDisplayName(contributedSettingClass)),
						ContributedProjectSettingPage.class,
						ContributedProjectSettingPage.paramsOf(getProject(), contributedSettingClass));
				var group = EditableUtils.getGroup(contributedSettingClass);
				if (group != null)
					menu.insertMenuItem(new SidebarMenuItem.SubMenu("sliders", _T("Settings"), Lists.newArrayList(new SidebarMenuItem.SubMenu(null, _T(group), Lists.newArrayList(menuItem)))));
				else
					menu.insertMenuItem(new SidebarMenuItem.SubMenu("sliders", _T("Settings"), Lists.newArrayList(menuItem)));
			}
		}

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

	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
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
				
				String queryString = getProjectManager().getFavoriteQuery();
				ProjectQuery query = ProjectQuery.parse(queryString);
				for (Project project: getProjectManager().query(query, false, 0, WebConstants.PAGE_SIZE)) 
					projectsView.add(newItem(projectsView.newChildId(), project));
				
				fragment.add(projectsView);
				
				fragment.add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {
					
					@Override
					protected void appendMore(AjaxRequestTarget target, int offset, int count) {
						for (Project project: getProjectManager().query(query, false, offset, count)) {
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
					
					if (item.getIndex() < getModelObject().size() - 1 || getProjectManager().hasChildren(project.getId())) {
						Long projectId = project.getId();
						item.add(new DropdownLink("children") {
	
							@Override
							protected Component newContent(String id, FloatingPanel dropdown) {
								return new ProjectChildrenTree(id, projectId) {

									@Override
									protected WebMarkupContainer newChildLink(String componentId, Long childId) {
										return navToProject(componentId, ProjectPage.getProjectManager().load(childId));
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
		
		fragment.add(newProjectTitle("projectTitle"));
		return fragment;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String description = getProject().getDescription();
		if(description == null || description.equals("")) {
			description = getProject().getName();
		}
		
		String urlOfProjectImage = getSettingManager().getSystemSetting().getServerUrl() +
				OneDev.getInstance(AvatarManager.class).getProjectAvatarUrl(getProject().getId());
		
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Title, getProject().getPath()).render(response);
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Description, description).render(response);
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Image, 
				urlOfProjectImage).render(response);
		new OpenGraphHeaderMeta(OpenGraphHeaderMetaType.Url, getProject().getUrl()).render(response);

		response.render(CssHeaderItem.forReference(new ProjectCssResourceReference()));
		response.render(CssHeaderItem.forReference(new DropdownTriangleIndicatorCssResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return getProject().getPath();
	}

	protected abstract BookmarkablePageLink<Void> navToProject(String componentId, Project project);
	
	protected static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	protected abstract Component newProjectTitle(String componentId);
	
	public static PageParameters paramsOf(Long projectId) {
		ProjectFacade project = getProjectManager().findFacadeById(projectId);
		return paramsOf(project.getPath());
	}
	
	public static PageParameters paramsOf(String projectPath) {
		PageParameters params = new PageParameters();
		params.add(ProjectMapperUtils.PARAM_PROJECT, projectPath);
		return params;
	}
	
	public static PageParameters paramsOf(Project project) {
		return paramsOf(project.getPath());
	}
	
}

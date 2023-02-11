package io.onedev.server.web.page.project;

import com.google.common.collect.Lists;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.dropdowntriangleindicator.DropdownTriangleIndicatorCssResourceReference;
import io.onedev.server.web.avatar.AvatarManager;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
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
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneEditPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneListPage;
import io.onedev.server.web.page.project.issues.milestones.NewMilestonePage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import io.onedev.server.web.page.project.setting.avatar.AvatarEditPage;
import io.onedev.server.web.page.project.setting.build.*;
import io.onedev.server.web.page.project.setting.code.analysis.CodeAnalysisSettingPage;
import io.onedev.server.web.page.project.setting.code.branchprotection.BranchProtectionsPage;
import io.onedev.server.web.page.project.setting.code.git.GitPackConfigPage;
import io.onedev.server.web.page.project.setting.code.pullrequest.PullRequestSettingPage;
import io.onedev.server.web.page.project.setting.code.tagprotection.TagProtectionsPage;
import io.onedev.server.web.page.project.setting.general.GeneralProjectSettingPage;
import io.onedev.server.web.page.project.setting.pluginsettings.ContributedProjectSettingPage;
import io.onedev.server.web.page.project.setting.servicedesk.ProjectServiceDeskSettingPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.stats.ProjectContribsPage;
import io.onedev.server.web.page.project.stats.SourceLinesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.util.ProjectAware;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
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

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@SuppressWarnings("serial")
public abstract class ProjectPage extends LayoutPage implements ProjectAware {

	protected final IModel<Project> projectModel;
	
	private transient Map<ObjectId, Collection<Build>> buildsCache;
	
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
		if (project == null)
			throw new EntityNotFoundException();

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
				&& getProject().getStorageServerUUID(false) == null) {
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
	protected boolean isPermitted() {
		return SecurityUtils.canAccess(getProject());
	}
	
	@Override
	public Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	protected List<SidebarMenu> getSidebarMenus() {
		List<SidebarMenu> menus = super.getSidebarMenus();
		
		List<SidebarMenuItem> menuItems = new ArrayList<>();
		
		if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject())) {
			List<SidebarMenuItem> codeMenuItems = new ArrayList<>();
	
			codeMenuItems.add(new SidebarMenuItem.Page(null, "Files", 
					ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject())));
			codeMenuItems.add(new SidebarMenuItem.Page(null, "Commits", 
					ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(getProject(), null), 
					Lists.newArrayList(CommitDetailPage.class)));
			codeMenuItems.add(new SidebarMenuItem.Page(null, "Branches", 
					ProjectBranchesPage.class, ProjectBranchesPage.paramsOf(getProject())));
			codeMenuItems.add(new SidebarMenuItem.Page(null, "Tags", 
					ProjectTagsPage.class, ProjectTagsPage.paramsOf(getProject())));
			codeMenuItems.add(new SidebarMenuItem.Page(null, "Pull Requests", 
					ProjectPullRequestsPage.class, ProjectPullRequestsPage.paramsOf(getProject(), 0), 
					Lists.newArrayList(NewPullRequestPage.class, PullRequestDetailPage.class, InvalidPullRequestPage.class)));
			codeMenuItems.add(new SidebarMenuItem.Page(null, "Code Comments", 
					ProjectCodeCommentsPage.class, ProjectCodeCommentsPage.paramsOf(getProject(), 0)));
			codeMenuItems.add(new SidebarMenuItem.Page(null, "Code Compare", 
					RevisionComparePage.class, RevisionComparePage.paramsOf(getProject())));
			
			menuItems.add(new SidebarMenuItem.SubMenu("git", "Code", codeMenuItems));
		}		
		if (getProject().isIssueManagement()) {
			List<SidebarMenuItem> issueMenuItems = new ArrayList<>();
			
			issueMenuItems.add(new SidebarMenuItem.Page(null, "List", 
					ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(getProject(), 0), 
					Lists.newArrayList(NewIssuePage.class, IssueDetailPage.class)));
			issueMenuItems.add(new SidebarMenuItem.Page(null, "Boards", 
					IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject())));
			issueMenuItems.add(new SidebarMenuItem.Page(null, "Milestones", 
					MilestoneListPage.class, MilestoneListPage.paramsOf(getProject(), false, null), 
					Lists.newArrayList(NewMilestonePage.class, MilestoneDetailPage.class, MilestoneEditPage.class)));
			
			menuItems.add(new SidebarMenuItem.SubMenu("bug", "Issues", issueMenuItems));
		}
		
		if (getProject().isCodeManagement()) {
			menuItems.add(new SidebarMenuItem.Page("play-circle", "Builds", 
					ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(getProject(), 0), 
					Lists.newArrayList(BuildDetailPage.class, InvalidBuildPage.class)));
		}
		
		List<SidebarMenuItem> statsMenuItems = new ArrayList<>();
		
		if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject())) {
			statsMenuItems.add(new SidebarMenuItem.Page(null, "Contributions", 
					ProjectContribsPage.class, ProjectContribsPage.paramsOf(getProject())));
			statsMenuItems.add(new SidebarMenuItem.Page(null, "Source Lines", 
					SourceLinesPage.class, SourceLinesPage.paramsOf(getProject())));
		}
		
		List<StatisticsMenuContribution> contributions = new ArrayList<>(OneDev.getExtensions(StatisticsMenuContribution.class));
		contributions.sort(Comparator.comparing(StatisticsMenuContribution::getOrder));
		
		for (StatisticsMenuContribution contribution: contributions)
			statsMenuItems.addAll(contribution.getMenuItems(getProject()));
		
		if (!statsMenuItems.isEmpty())
			menuItems.add(new SidebarMenuItem.SubMenu("statistics", "Statistics", statsMenuItems));
		
		menuItems.add(new SidebarMenuItem.Page("tree", "Child Projects", 
				ProjectChildrenPage.class, ProjectChildrenPage.paramsOf(getProject(), null, 0)));
		
		if (SecurityUtils.canManage(getProject())) {
			List<SidebarMenuItem> settingMenuItems = new ArrayList<>();
			settingMenuItems.add(new SidebarMenuItem.Page(null, "General Settings", 
					GeneralProjectSettingPage.class, GeneralProjectSettingPage.paramsOf(getProject())));
			settingMenuItems.add(new SidebarMenuItem.Page(null, "Edit Avatar", 
					AvatarEditPage.class, AvatarEditPage.paramsOf(getProject())));
			settingMenuItems.add(new SidebarMenuItem.Page(null, "Authorizations", 
					ProjectAuthorizationsPage.class, ProjectAuthorizationsPage.paramsOf(getProject())));

			List<SidebarMenuItem> codeSettingMenuItems = new ArrayList<>();
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, "Branch Protection", 
					BranchProtectionsPage.class, BranchProtectionsPage.paramsOf(getProject())));
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, "Tag Protection", 
					TagProtectionsPage.class, TagProtectionsPage.paramsOf(getProject())));
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, "Code Analysis", 
					CodeAnalysisSettingPage.class, CodeAnalysisSettingPage.paramsOf(getProject())));
			if (getProject().isCodeManagement()) {
				codeSettingMenuItems.add(new SidebarMenuItem.Page(null, "Git Pack Config",
						GitPackConfigPage.class, GitPackConfigPage.paramsOf(getProject())));
			}
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, "Pull Request",
					PullRequestSettingPage.class, PullRequestSettingPage.paramsOf(getProject())));
			
			settingMenuItems.add(new SidebarMenuItem.SubMenu(null, "Code", codeSettingMenuItems));
			
			List<SidebarMenuItem> buildSettingMenuItems = new ArrayList<>();
			
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, "Job Secrets", 
					JobSecretsPage.class, JobSecretsPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, "Job Properties",
					JobPropertiesPage.class, JobPropertiesPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, "Action Authorizations", 
					ActionAuthorizationsPage.class, ActionAuthorizationsPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, "Build Preserve Rules", 
					BuildPreservationsPage.class, BuildPreservationsPage.paramsOf(getProject())));
			buildSettingMenuItems.add(new SidebarMenuItem.Page(null, "Default Fixed Issue Filters", 
					DefaultFixedIssueFiltersPage.class, DefaultFixedIssueFiltersPage.paramsOf(getProject())));
			
			settingMenuItems.add(new SidebarMenuItem.SubMenu(null, "Build", buildSettingMenuItems));

			if (getSettingManager().getServiceDeskSetting() != null && getProject().isIssueManagement()) {
				settingMenuItems.add(new SidebarMenuItem.Page(null, "Service Desk", 
						ProjectServiceDeskSettingPage.class, ProjectServiceDeskSettingPage.paramsOf(getProject())));
			}
			
			settingMenuItems.add(new SidebarMenuItem.Page(null, "Web Hooks", 
					WebHooksPage.class, WebHooksPage.paramsOf(getProject())));
			
			List<Class<? extends ContributedProjectSetting>> contributedSettingClasses = new ArrayList<>();
			for (ProjectSettingContribution contribution:OneDev.getExtensions(ProjectSettingContribution.class)) {
				for (Class<? extends ContributedProjectSetting> settingClass: contribution.getSettingClasses()) 
					contributedSettingClasses.add(settingClass);
			}
			contributedSettingClasses.sort(Comparator.comparingInt(EditableUtils::getOrder));
			
			Map<String, List<SidebarMenuItem>> contributedSettingMenuItems = new HashMap<>();
			for (var contributedSettingClass: contributedSettingClasses) {
				var group = EditableUtils.getGroup(contributedSettingClass);
				if (group == null)
					group = "";
				var contributedSettingMenuItemsOfGroup = contributedSettingMenuItems.get(group);
				if (contributedSettingMenuItemsOfGroup == null) {
					contributedSettingMenuItemsOfGroup = new ArrayList<>();
					contributedSettingMenuItems.put(group, contributedSettingMenuItemsOfGroup);
				}
				contributedSettingMenuItemsOfGroup.add(new SidebarMenuItem.Page(
						null,
						EditableUtils.getDisplayName(contributedSettingClass),
						ContributedProjectSettingPage.class,
						ContributedProjectSettingPage.paramsOf(getProject(), contributedSettingClass)));
			}
			for (var entry: contributedSettingMenuItems.entrySet()) {
				if (entry.getKey().length() == 0) {
					settingMenuItems.addAll(entry.getValue());
				} else {
					settingMenuItems.add(new SidebarMenuItem.SubMenu(null, entry.getKey(), entry.getValue()));
				}
			}
			
			menuItems.add(new SidebarMenuItem.SubMenu("sliders", "Settings", settingMenuItems));
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
		menus.add(new SidebarMenu(menuHeader, menuItems));
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
				for (Project project: getProjectManager().query(query, 0, WebConstants.PAGE_SIZE)) 
					projectsView.add(newItem(projectsView.newChildId(), project));
				
				fragment.add(projectsView);
				
				fragment.add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {
					
					@Override
					protected void appendMore(AjaxRequestTarget target, int offset, int count) {
						for (Project project: getProjectManager().query(query, offset, count)) {
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
				if (SecurityUtils.canAccess(project)) {
					WebMarkupContainer link = navToProject("link", project);
					link.add(new Label("label", project.getName()));
					item.add(link);
					
					List<ProjectFacade> children = getProjectManager().getChildren(project.getId());
					if (!children.isEmpty()) {
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

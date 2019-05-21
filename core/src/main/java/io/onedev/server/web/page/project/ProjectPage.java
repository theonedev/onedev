package io.onedev.server.web.page.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.cache.UserInfoManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.component.sidebar.SideBar;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.codecomments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.info.ProjectInfoPanel;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.list.IssueListPage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.page.project.setting.ProjectSettingTab;
import io.onedev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import io.onedev.server.web.page.project.setting.avatar.AvatarEditPage;
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import io.onedev.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.setting.issue.PromptFieldsUponIssueOpenSettingPage;
import io.onedev.server.web.page.project.setting.issue.StateTransitionsPage;
import io.onedev.server.web.page.project.setting.secret.SecretListPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.stats.ProjectContribsPage;
import io.onedev.server.web.page.project.stats.ProjectStatsPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.util.ProjectAware;

@SuppressWarnings("serial")
public abstract class ProjectPage extends LayoutPage implements ProjectAware {

	private static final String PARAM_PROJECT = "project";
	
	protected final IModel<Project> projectModel;
	
	public static PageParameters paramsOf(Project project) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getName());
		return params;
	}
	
	public ProjectPage(PageParameters params) {
		super(params);
		
		String projectName = params.get(PARAM_PROJECT).toString();
		Preconditions.checkNotNull(projectName);
		
		Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
		
		if (project == null) 
			throw new OneException("Unable to find project " + projectName);
		
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
		
		// we do not need to reload the project this time as we already have that object on hand
		projectModel.setObject(project);
		
		if (!(this instanceof NoCommitsPage) 
				&& SecurityUtils.canReadCode(getProject().getFacade())
				&& !(this instanceof ProjectSettingPage) 
				&& getProject().getDefaultBranch() == null) { 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getProject()));
		}
	}
	
	protected Map<String, ObjectId> getObjectIdCache() {
		return new HashMap<>();
	}
	
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		if (getLoginUser() != null)
			OneDev.getInstance(UserInfoManager.class).visit(getLoginUser(), getProject());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new SideBar("sidebar", "project.miniSidebar") {
			
			@Override
			protected List<? extends Tab> newTabs() {
				List<ProjectTab> tabs = ProjectPage.this.newTabs();
				if (SecurityUtils.canAdministrate(getProject().getFacade()))
					tabs.add(new ProjectTab(Model.of("Setting"), "fa fa-fw fa-cog", 0, GeneralSettingPage.class, ProjectSettingPage.class));
				return tabs;
			}

			@Override
			protected Component newHead(String componentId) {
				Fragment fragment = new Fragment(componentId, "sidebarHeadFrag", ProjectPage.this);
				Project project = getProject();
				AlignPlacement placement = new AlignPlacement(0, 100, 0, 0);
				AjaxLink<Void> link = new DropdownLink("link", placement) {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new ProjectInfoPanel(id, projectModel) {
							
							@Override
							protected void onPromptForkOption(AjaxRequestTarget target) {
								dropdown.close();
							}
						};
					}
					
				};
				link.add(new ProjectAvatar("avatar", getProject()));
				link.add(new Label("name", project.getName()));
				fragment.add(link);
				
				return fragment;
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(new ProjectResourceReference()));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadIssues(getProject().getFacade());
	}
	
	@Override
	public Project getProject() {
		return projectModel.getObject();
	}
	
	private List<ProjectTab> newTabs() {
		List<ProjectTab> tabs = new ArrayList<>();
		if (SecurityUtils.canReadCode(getProject().getFacade())) {
			tabs.add(new ProjectTab(Model.of("Files"), "fa fa-fw fa-file-text-o", 0, ProjectBlobPage.class));
			tabs.add(new ProjectTab(Model.of("Commits"), "fa fa-fw fa-ext fa-commit", 0,
					ProjectCommitsPage.class, CommitDetailPage.class) {
				
				@Override
				public Component render(String componentId) {
					return new ProjectTabLink(componentId, this) {

						@Override
						protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
							return new ViewStateAwarePageLink<Void>(linkId, ProjectCommitsPage.class, 
									ProjectCommitsPage.paramsOf(getProject(), "", null));
						}
					};
				}
				
			});
			tabs.add(new ProjectTab(Model.of("Branches"), "fa fa-fw fa-code-fork", 
					0, ProjectBranchesPage.class));
			tabs.add(new ProjectTab(Model.of("Tags"), "fa fa-fw fa-tag", 
					0, ProjectTagsPage.class));
			
			tabs.add(new ProjectTab(Model.of("Pull Requests"), "fa fa-fw fa-ext fa-branch-compare", 
					0, ProjectPullRequestsPage.class, NewPullRequestPage.class, PullRequestDetailPage.class, InvalidPullRequestPage.class) {
				
				@Override
				public Component render(String componentId) {
					return new ProjectTabLink(componentId, this) {

						@Override
						protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
							return new ViewStateAwarePageLink<Void>(linkId, ProjectPullRequestsPage.class, 
									ProjectPullRequestsPage.paramsOf(getProject(), "", 0));
						}
					};
				}
				
			});
		}
		
		tabs.add(new ProjectTab(Model.of("Issues"), "fa fa-fw fa-bug", 0, IssueListPage.class, ProjectIssuesPage.class, 
				IssueDetailPage.class, NewIssuePage.class) {

			@Override
			public Component render(String componentId) {
				return new ProjectTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new ViewStateAwarePageLink<Void>(linkId, IssueListPage.class, 
								IssueListPage.paramsOf(getProject(), "", 0));
					}
				};
			}
			
		});
		
		tabs.add(new ProjectTab(Model.of("Builds"), "fa fa-fw fa-cubes", 0, ProjectBuildsPage.class, BuildDetailPage.class) {

			@Override
			public Component render(String componentId) {
				return new ProjectTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new ViewStateAwarePageLink<Void>(linkId, ProjectBuildsPage.class, 
								ProjectBuildsPage.paramsOf(getProject(), "", 0));
					}
				};
			}
			
		});
		
		if (SecurityUtils.canReadCode(getProject().getFacade())) {
			tabs.add(new ProjectTab(Model.of("Code Comments"), "fa fa-fw fa-comments", 
					0, ProjectCodeCommentsPage.class) {

				@Override
				public Component render(String componentId) {
					return new ProjectTabLink(componentId, this) {

						@Override
						protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
							return new ViewStateAwarePageLink<Void>(linkId, ProjectCodeCommentsPage.class, 
									ProjectCodeCommentsPage.paramsOf(getProject(), ""));
						}
					};
				}
				
			});
			tabs.add(new ProjectTab(Model.of("Compare"), "fa fa-fw fa-ext fa-file-diff", 0, RevisionComparePage.class));
			tabs.add(new ProjectTab(Model.of("Statistics"), "fa fa-fw fa-bar-chart", 0, ProjectContribsPage.class, 
					ProjectStatsPage.class));
		}
		
		return tabs;		
	}
	
	@Override
	protected Component newNavContext(String componentId) {
		Fragment fragment = new Fragment(componentId, "navContextFrag", this);
		DropdownLink link = new DropdownLink("dropdown", AlignPlacement.bottom(15)) {

			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				dropdown.add(AttributeAppender.append("class", "nav-context-dropdown project-nav-context-dropdown"));
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "navContextDropdownFrag", ProjectPage.this);
				fragment.add(new ProjectInfoPanel("info", projectModel) {
					
					@Override
					protected void onPromptForkOption(AjaxRequestTarget target) {
						dropdown.close();
					}
					
				});
				fragment.add(new ListView<ProjectTab>("items", newTabs()) {

					@Override
					protected void populateItem(ListItem<ProjectTab> item) {
						ProjectTab tab = item.getModelObject();
						item.add(tab.render("item"));
						if (tab.isActive(getPage()))
							item.add(AttributeAppender.append("class", "active"));
					}
					
				});
				WebMarkupContainer settingItem = new WebMarkupContainer("setting");
				settingItem.setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
				settingItem.add(new Tabbable("menu", newSettingTabs()));
				if (getPage() instanceof ProjectSettingPage) 
					settingItem.add(AttributeAppender.append("class", "active expanded"));
				fragment.add(settingItem);
				return fragment;
			}
			
		};
		link.add(new ProjectAvatar("avatar", getProject()));
		link.add(new Label("name", getProject().getName()));
		fragment.add(link);
		
		return fragment;
	}
	
	protected List<ProjectSettingTab> newSettingTabs() {
		List<ProjectSettingTab> tabs = new ArrayList<>();
		tabs.add(new ProjectSettingTab("General Setting", "fa fa-fw fa-sliders", GeneralSettingPage.class));
		tabs.add(new ProjectSettingTab("Edit Avatar", "fa fa-fw fa-picture-o", AvatarEditPage.class));
		tabs.add(new ProjectSettingTab("Authorizations", "fa fa-fw fa-user", ProjectAuthorizationsPage.class));
		tabs.add(new ProjectSettingTab("Branch Protection", "fa fa-fw fa-lock", BranchProtectionPage.class));
		tabs.add(new ProjectSettingTab("Tag Protection", "fa fa-fw fa-lock", TagProtectionPage.class));
		tabs.add(new ProjectSettingTab("Secrets", "fa fa-fw fa-key", SecretListPage.class));
		tabs.add(new ProjectSettingTab("Issue Setting", "fa fa-fw fa-bug", StateTransitionsPage.class, PromptFieldsUponIssueOpenSettingPage.class));
		tabs.add(new ProjectSettingTab("Commit Message Transform", "fa fa-fw fa-comments", CommitMessageTransformPage.class));
		tabs.add(new ProjectSettingTab("Web Hooks", "fa fa-fw fa-volume-up", WebHooksPage.class));
		return tabs;
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

}

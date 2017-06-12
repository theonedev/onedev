package com.gitplex.server.web.page.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.UserInfoManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.tabbable.PageTab;
import com.gitplex.server.web.component.tabbable.Tabbable;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.branches.ProjectBranchesPage;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.server.web.page.project.commit.ProjectCommitsPage;
import com.gitplex.server.web.page.project.compare.RevisionComparePage;
import com.gitplex.server.web.page.project.moreinfo.MoreInfoPanel;
import com.gitplex.server.web.page.project.pullrequest.newrequest.NewRequestPage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.RequestDetailPage;
import com.gitplex.server.web.page.project.pullrequest.requestlist.RequestListPage;
import com.gitplex.server.web.page.project.setting.ProjectSettingPage;
import com.gitplex.server.web.page.project.setting.general.GeneralSettingPage;
import com.gitplex.server.web.page.project.tags.ProjectTagsPage;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public abstract class ProjectPage extends LayoutPage {

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
		
		if (projectName.endsWith(Constants.DOT_GIT_EXT))
			projectName = projectName.substring(0, projectName.length() - Constants.DOT_GIT_EXT.length());
		
		Project project = GitPlex.getInstance(ProjectManager.class).find(projectName);
		
		if (project == null) 
			throw new EntityNotFoundException("Unable to find project " + projectName);
		
		Long projectId = project.getId();
		
		projectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				Project project = GitPlex.getInstance(ProjectManager.class).load(projectId);
				
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
		
		if (!(this instanceof NoBranchesPage) 
				&& !(this instanceof ProjectSettingPage) 
				&& getProject().getDefaultBranch() == null) { 
			throw new RestartResponseException(NoBranchesPage.class, paramsOf(getProject()));
		}
	}
	
	protected Map<String, ObjectId> getObjectIdCache() {
		return new HashMap<>();
	}
	
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		if (getLoginUser() != null)
			GitPlex.getInstance(UserInfoManager.class).visit(getLoginUser(), getProject());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new ProjectTab(Model.of("Files"), "fa fa-fw fa-file-text-o", 0, ProjectBlobPage.class));
		tabs.add(new ProjectTab(Model.of("Commits"), "fa fa-fw fa-ext fa-commit", 0,
				ProjectCommitsPage.class, CommitDetailPage.class));
		tabs.add(new ProjectTab(Model.of("Branches"), "fa fa-fw fa-code-fork", 
				0, ProjectBranchesPage.class));
		tabs.add(new ProjectTab(Model.of("Tags"), "fa fa-fw fa-tag", 
				0, ProjectTagsPage.class));
		
		tabs.add(new ProjectTab(Model.of("Pull Requests"), "fa fa-fw fa-ext fa-branch-compare", 
				0, RequestListPage.class, NewRequestPage.class, RequestDetailPage.class));
		
		tabs.add(new ProjectTab(Model.of("Compare"), "fa fa-fw fa-ext fa-file-diff", 0, RevisionComparePage.class));
		
		if (SecurityUtils.canManage(getProject()))
			tabs.add(new ProjectTab(Model.of("Setting"), "fa fa-fw fa-cog", 0, GeneralSettingPage.class, ProjectSettingPage.class));
		
		WebMarkupContainer sidebar = new WebMarkupContainer("sidebar");
		add(sidebar);
		
		/*
		 * Add mini class here instead of project.js as we want the sidebar to 
		 * be minimized initially even if the page takes some time to load 
		 */
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie("project.miniSidebar");
		if (cookie != null && "yes".equals(cookie.getValue()))
			sidebar.add(AttributeAppender.append("class", " mini"));
		
		sidebar.add(new Tabbable("projectTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new ProjectResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("gitplex.server.project();"));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canRead(getProject());
	}
	
	public Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
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

		breadcrumbs.add(new ComponentRenderer() {
			
			@Override
			public Component render(String componentId) {
				Fragment fragment = new Fragment(componentId, "breadcrumbFrag", ProjectPage.this) {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("div");
					}
					
				};
				fragment.add(new BookmarkablePageLink<Void>("name", ProjectBlobPage.class, 
						ProjectBlobPage.paramsOf(getProject())) {

					@Override
					public IModel<?> getBody() {
						return Model.of(getProject().getName());
					}
					
				});
				fragment.add(new DropdownLink("moreInfo") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new MoreInfoPanel(id, projectModel) {

							@Override
							protected void onPromptForkOption(AjaxRequestTarget target) {
								dropdown.close();
							}
							
						};
					}
					
				});
				return fragment;
			}
			
		});
		
		return breadcrumbs;
	}

}

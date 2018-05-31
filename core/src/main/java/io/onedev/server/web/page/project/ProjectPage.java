package io.onedev.server.web.page.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ComponentRenderer;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.FirstIssueQueryLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.sidebar.SideBar;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.comments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.issues.IssuesPage;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.page.project.moreinfo.MoreInfoPanel;
import io.onedev.server.web.page.project.pullrequests.InvalidRequestPage;
import io.onedev.server.web.page.project.pullrequests.newrequest.NewRequestPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.RequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.requestlist.RequestListPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;

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
		
		Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
		
		if (project == null) 
			throw new EntityNotFoundException("Unable to find project " + projectName);
		
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
				List<PageTab> tabs = new ArrayList<>();
				tabs.add(new ProjectTab(Model.of("Files"), "fa fa-fw fa-file-text-o", 0, ProjectBlobPage.class));
				tabs.add(new ProjectTab(Model.of("Commits"), "fa fa-fw fa-ext fa-commit", 0,
						ProjectCommitsPage.class, CommitDetailPage.class));
				tabs.add(new ProjectTab(Model.of("Branches"), "fa fa-fw fa-code-fork", 
						0, ProjectBranchesPage.class));
				tabs.add(new ProjectTab(Model.of("Tags"), "fa fa-fw fa-tag", 
						0, ProjectTagsPage.class));
				
				tabs.add(new ProjectTab(Model.of("Pull Requests"), "fa fa-fw fa-ext fa-branch-compare", 
						0, RequestListPage.class, NewRequestPage.class, RequestDetailPage.class, InvalidRequestPage.class));
				
				tabs.add(new ProjectTab(Model.of("Issues"), "fa fa-fw fa-bug", 0, IssueListPage.class, IssuesPage.class, 
						IssueDetailPage.class, NewIssuePage.class) {

					@Override
					public Component render(String componentId) {
						return new ProjectTabLink(componentId, this) {

							@Override
							protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
								return new FirstIssueQueryLink(linkId, getProject());
							}
						};
					}
					
				});
				
				tabs.add(new ProjectTab(Model.of("Code Comments"), "fa fa-fw fa-comments", 
						0, ProjectCodeCommentsPage.class));
				
				tabs.add(new ProjectTab(Model.of("Compare"), "fa fa-fw fa-ext fa-file-diff", 0, RevisionComparePage.class));
				
				/*
				tabs.add(new ProjectTab(Model.of("Statistics"), "fa fa-fw fa-bar-chart", 0, ProjectContribsPage.class, 
						ProjectStatsPage.class));
				*/
				
				if (SecurityUtils.canManage(getProject()))
					tabs.add(new ProjectTab(Model.of("Setting"), "fa fa-fw fa-cog", 0, GeneralSettingPage.class, ProjectSettingPage.class));
				
				return tabs;
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
					
				}.add(AttributeAppender.append("title", getProject().getName())));
				
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

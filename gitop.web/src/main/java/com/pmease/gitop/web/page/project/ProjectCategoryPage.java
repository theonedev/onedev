package com.pmease.gitop.web.page.project;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.SessionData;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.api.ProjectPageTab;
import com.pmease.gitop.web.page.project.api.ProjectPageTab.Category;
import com.pmease.gitop.web.page.project.pullrequest.ClosedRequestsPage;
import com.pmease.gitop.web.page.project.pullrequest.NewRequestPage;
import com.pmease.gitop.web.page.project.pullrequest.OpenRequestsPage;
import com.pmease.gitop.web.page.project.pullrequest.RequestDetailPage;
import com.pmease.gitop.web.page.project.settings.ProjectOptionsPage;
import com.pmease.gitop.web.page.project.source.AbstractFilePage;
import com.pmease.gitop.web.page.project.source.ProjectHomePage;
import com.pmease.gitop.web.page.project.source.branches.BranchesPage;
import com.pmease.gitop.web.page.project.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.project.source.commits.CommitsPage;
import com.pmease.gitop.web.page.project.source.contributors.ContributorsPage;
import com.pmease.gitop.web.page.project.source.tags.TagsPage;
import com.pmease.gitop.web.page.project.source.tree.SourceTreePage;
import com.pmease.gitop.web.page.project.stats.ProjectForksPage;
import com.pmease.gitop.web.page.project.stats.ProjectGraphsPage;
import com.pmease.gitop.web.page.project.wiki.ProjectWikiPage;

@SuppressWarnings("serial")
public abstract class ProjectCategoryPage extends AbstractProjectPage {

	protected final IModel<String> revisionModel;
	
	public ProjectCategoryPage(PageParameters params) {
		super(params);

		String rev = findRevision(params);
		onUpdateRevision(rev);
		revisionModel = Model.of(rev);	
	}

	protected void onUpdateRevision(String rev) {
		if (!Objects.equal(rev, SessionData.get().getRevision())) {
			SessionData.get().onRevisionChanged();
		}
		
		SessionData.get().setRevision(rev);
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		add(createSidebar("sidebar"));
		add(new EmptyRepositoryPanel("nocommit", projectModel));
	}
	
	protected Component createSidebar(String id) {
		WebMarkupContainer sidebar = new WebMarkupContainer(id);
		
		Loop groups = new Loop("groups", Category.values().length) {

			@Override
			protected void populateItem(LoopItem item) {
				Category g = Category.values()[item.getIndex()];
				item.add(newGroupHead("name", g));
				item.add(newGroupNavs("nav", g));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisibilityAllowed(getProject().code().hasCommits());
			}
		};
		
		sidebar.add(groups);
		
		AbstractLink adminLink = new BookmarkablePageLink<Void>("settinglink", 
				ProjectOptionsPage.class, PageSpec.forProject(getProject())) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(getProject())));
			}
		};
		
		sidebar.add(adminLink);
		return sidebar;
	}
	
	protected String findRevision(PageParameters params) {
		String rev = params.get(PageSpec.OBJECT_ID).toString();
		if (Strings.isNullOrEmpty(rev)) {
			rev = SessionData.get().getRevision();
		}
		
		if (Strings.isNullOrEmpty(rev)) {
			rev = getProject().code().resolveDefaultBranch();
		}
		
		Git git = getProject().code();
		String hash = git.parseRevision(rev, false);
		if (hash == null) {
			throw new EntityNotFoundException("Ref " + rev + " doesn't exist");
		}
		
		return Preconditions.checkNotNull(rev);
	}
	
	private Component newGroupHead(String id, Category group) {
		// Replace underscore with space in order to display category 
		// PULL_REQUESTS as "PULL REQUESTS" 
		return new Label(id, group.name().replace("_", " "));
	}
	
	private List<ProjectPageTab> getTabs(final Category group) {
		return ImmutableList.<ProjectPageTab>copyOf(
				Iterables.filter(getAllTabs(), new Predicate<ProjectPageTab>() {

			@Override
			public boolean apply(ProjectPageTab input) {
				return Objects.equal(group.name(), input.getGroupName());
			}
			
		}));
	}
	
	@SuppressWarnings("unchecked")
	private List<ProjectPageTab> getAllTabs() {
		List<ProjectPageTab> tabs = Lists.newArrayList();
		
		// SOURCE TABS
		//
		tabs.add(new ProjectPageTab(Model.of("Code"), 
									Category.SOURCE, 
									"icon-code", 
									new Class[] { SourceTreePage.class, 
												  AbstractFilePage.class }) {
			@Override
			public Class<? extends Page> getBookmarkablePageClass() {
				String rev = SessionData.get().getRevision();
				if (Strings.isNullOrEmpty(rev)) {
					return ProjectHomePage.class;
				} else {
					return SourceTreePage.class;
				}
			}
		});
		
		tabs.add(new ProjectPageTab(Model.of("Commits"), 
									Category.SOURCE, 
									"icon-commits", 
									new Class[] { CommitsPage.class, 
												  SourceCommitPage.class }));
		
		tabs.add(new ProjectPageTab(Model.of("Branches"), 
									Category.SOURCE, 
									"icon-git-branch", 
									BranchesPage.class));
		
		tabs.add(new ProjectPageTab(Model.of("Tags"), 
									Category.SOURCE, 
									"icon-tags", 
									TagsPage.class));
		
		tabs.add(new ProjectPageTab(Model.of("Contributors"), 
									Category.SOURCE, 
									"icon-group-o", 
									ContributorsPage.class));
		
		// PULL REQUESTS TABS
		tabs.add(new ProjectPageTab(Model.of("Open"), 
									Category.PULL_REQUESTS, 
									"icon-pull-request", 
									OpenRequestsPage.class) {

										@Override
										public boolean isSelected(Page page) {
											if (page instanceof RequestDetailPage) {
												RequestDetailPage detailPage = (RequestDetailPage) page;
												if (detailPage.getPullRequest().isOpen())
													return true;
											}
											return super.isSelected(page);
										}
			
		});
		tabs.add(new ProjectPageTab(Model.of("Closed"), 
									Category.PULL_REQUESTS, 
									"icon-pull-request-abandon", 
									ClosedRequestsPage.class) {
			
										@Override
										public boolean isSelected(Page page) {
											if (page instanceof RequestDetailPage) {
												RequestDetailPage detailPage = (RequestDetailPage) page;
												if (!detailPage.getPullRequest().isOpen())
													return true;
											}
											return super.isSelected(page);
										}
			
		});
		
		tabs.add(new ProjectPageTab(Model.of("Create"), 
									Category.PULL_REQUESTS, 
									"icon-pull-request", 
									NewRequestPage.class));
		
		// WIKI TABS
		tabs.add(new ProjectPageTab(Model.of("Wiki"), 
									Category.WIKI, 
									"icon-wiki", 
									ProjectWikiPage.class));
		
		// STATISTICS TABS
		tabs.add(new ProjectPageTab(Model.of("Graphs"), 
									Category.STATISTICS, 
									"icon-chart-area", 
									ProjectGraphsPage.class));
		
		tabs.add(new ProjectPageTab(Model.of("Forks"), 
									Category.STATISTICS, 
									"icon-network", 
									ProjectForksPage.class));
		
		return tabs;
	} 

	private Component newGroupNavs(String id, Category group) {
		ListView<ProjectPageTab> tabs = new ListView<ProjectPageTab>(id, getTabs(group)) {
			@Override
			protected void populateItem(ListItem<ProjectPageTab> item) {
				final ProjectPageTab tab = item.getModelObject();
				
				item.add(tab.newTabLink("link"));
				
				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return tab.isSelected(getPage()) ? "active" : "";
					}
				}));
			}
		};
		
		return tabs;
	}
	
	protected boolean isRevisionAware() {
		return true;
	}
	
	protected String getRevision() {
		return revisionModel.getObject();
	}

	@Override
	public void onDetach() {
		if (revisionModel != null) {
			revisionModel.detach();
		}
		
		super.onDetach();
	}
}

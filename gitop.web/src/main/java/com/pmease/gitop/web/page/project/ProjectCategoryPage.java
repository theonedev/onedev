package com.pmease.gitop.web.page.project;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.api.ProjectPageTab;
import com.pmease.gitop.web.page.project.api.ProjectPageTab.Category;
import com.pmease.gitop.web.page.project.issue.ProjectPullRequestsPage;
import com.pmease.gitop.web.page.project.settings.ProjectOptionsPage;
import com.pmease.gitop.web.page.project.source.AbstractFilePage;
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
		
		revisionModel = new LoadableDetachableModel<String>() {

			@Override
			public String load() {
				return findRevision();
			}
		};
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		Loop groups = new Loop("groups", Category.values().length) {

			@Override
			protected void populateItem(LoopItem item) {
				Category g = Category.values()[item.getIndex()];
				item.add(newGroupHead("name", g));
				item.add(newGroupNavs("nav", g));
			}
		};
		
		add(groups);
		
		AbstractLink adminLink = new BookmarkablePageLink<Void>("settinglink", 
				ProjectOptionsPage.class, PageSpec.forProject(getProject())) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(getProject())));
			}
		};
		
		add(adminLink);
	}
	
	protected String findRevision() {
		PageParameters params = getPageParameters();
		String objectId = params.get(PageSpec.OBJECT_ID).toString();
		String rev;
		Project project = getProject();
		if (Strings.isNullOrEmpty(objectId)) {
			String branchName = project.getDefaultBranchName();
			if (Strings.isNullOrEmpty(branchName)) {
				Git git = project.code();
				List<String> branches = Lists.newArrayList(git.listBranches());
				if (branches.contains(Constants.MASTER)) {
					rev = Constants.MASTER;
				} else {
					rev = Iterables.getFirst(branches, null);
				}
			} else {
				rev = branchName;
			}
		} else {
			rev = objectId;
		}

		Preconditions.checkState(rev != null);
		
		Git git = getProject().code();
		String hash = git.parseRevision(rev, false);
		if (hash == null) {
			throw new EntityNotFoundException("Ref " + rev + " doesn't exist");
		} else {
			return rev;
		}
	}
	
	private Component newGroupHead(String id, Category group) {
		return new Label(id, group.name());
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
		tabs.add(new ProjectPageTab(Model.of("Code"), Category.SOURCE, "icon-code", new Class[] { SourceTreePage.class, AbstractFilePage.class }));
		tabs.add(new ProjectPageTab(Model.of("Commits"), Category.SOURCE, "icon-commits", new Class[] { CommitsPage.class, SourceCommitPage.class }));
		tabs.add(new ProjectPageTab(Model.of("Branches"), Category.SOURCE, "icon-git-branch", BranchesPage.class));
		tabs.add(new ProjectPageTab(Model.of("Tags"), Category.SOURCE, "icon-tags", TagsPage.class));
		tabs.add(new ProjectPageTab(Model.of("Contributors"), Category.SOURCE, "icon-group-o", ContributorsPage.class));
		
		// WIKI TABS
		tabs.add(new ProjectPageTab(Model.of("Wiki"), Category.WIKI, "icon-wiki", ProjectWikiPage.class));
		
		// ISSUES TABS
		tabs.add(new ProjectPageTab(Model.of("Pull Requests"), Category.ISSUES, "icon-pull-request", ProjectPullRequestsPage.class));
		
		// STATISTICS TABS
		tabs.add(new ProjectPageTab(Model.of("Graphs"), Category.STATISTICS, "icon-chart-area", ProjectGraphsPage.class));
		tabs.add(new ProjectPageTab(Model.of("Forks"), Category.STATISTICS, "icon-network", ProjectForksPage.class));
		
		return tabs;
	} 

	private Component newGroupNavs(String id, Category group) {
		ListView<ProjectPageTab> tabs = new ListView<ProjectPageTab>(id, getTabs(group)) {
			@Override
			protected void populateItem(ListItem<ProjectPageTab> item) {
				final ProjectPageTab tab = item.getModelObject();
				item.add(tab.newTabLink("link", projectModel, revisionModel));
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

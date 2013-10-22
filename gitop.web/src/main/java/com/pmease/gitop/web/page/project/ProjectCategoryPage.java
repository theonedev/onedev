package com.pmease.gitop.web.page.project;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.api.ProjectTabContribution;
import com.pmease.gitop.web.page.project.api.ProjectTabGroup;
import com.pmease.gitop.web.page.project.issue.ProjectMergeRequestsPage;
import com.pmease.gitop.web.page.project.source.ProjectHomePage;
import com.pmease.gitop.web.page.project.source.RepositoryBranchesPage;
import com.pmease.gitop.web.page.project.source.RepositoryCommitsPage;
import com.pmease.gitop.web.page.project.source.RepositoryContributorsPage;
import com.pmease.gitop.web.page.project.source.RepositoryTagsPage;
import com.pmease.gitop.web.page.project.stats.ProjectForksPage;
import com.pmease.gitop.web.page.project.stats.ProjectGraphsPage;
import com.pmease.gitop.web.page.project.wiki.ProjectWikiPage;

@SuppressWarnings("serial")
public abstract class ProjectCategoryPage extends AbstractProjectPage {

	public static enum Category implements ProjectTabContribution {
		CODE(ProjectTabGroup.SOURCE, "Code", ProjectHomePage.class, "icon-code") {
			@Override
			Component createBadge(String id, IModel<Project> project) {
				return new WebMarkupContainer(id).setVisibilityAllowed(false);
			}
		},
		
		COMMITS(ProjectTabGroup.SOURCE, "Commits", RepositoryCommitsPage.class, "icon-time"),
		BRANCHES(ProjectTabGroup.SOURCE, "Branches", RepositoryBranchesPage.class, "icon-code-fork"),
		TAGS(ProjectTabGroup.SOURCE, "Tags", RepositoryTagsPage.class, "icon-tag"),
		CONTRIBUTORS(ProjectTabGroup.SOURCE, "Contributors", RepositoryContributorsPage.class, "icon-group"),
		WIKI(ProjectTabGroup.WIKI, "Wiki", ProjectWikiPage.class, "icon-wiki") {
			@Override
			Component createBadge(String id, IModel<Project> project) {
				return new WebMarkupContainer(id).setVisibilityAllowed(false);
			}
		},
		
		MERGE_REQUESTS(ProjectTabGroup.ISSUES, "Merge Requests", ProjectMergeRequestsPage.class, "icon-repo-merge"),
		
		GRAPHS(ProjectTabGroup.STATISTICS, "Graphs", ProjectGraphsPage.class, "icon-chart-area") {
			@Override
			Component createBadge(String id, IModel<Project> project) {
				return new WebMarkupContainer(id).setVisibilityAllowed(false);
			}
		},
		
		FORKS(ProjectTabGroup.STATISTICS, "Forks", ProjectForksPage.class, "icon-network") {
			@Override
			Component createBadge(String id, IModel<Project> project) {
				return new WebMarkupContainer(id).setVisibilityAllowed(false);
			}
		};
		
		final ProjectTabGroup group;
		final String displayName;
		final Class<? extends Page> pageClass;
		final String icon;
		
		Category(final ProjectTabGroup group, 
				 final String displayName, 
				 final Class<? extends Page> pageClass, 
				 final String icon) {
			this.group = group;
			this.displayName = displayName;
			this.pageClass = pageClass;
			this.icon = icon;
		}

		@Override
		public ProjectTabGroup getGroup() {
			return group;
		}

		@Override
		public String getName() {
			return displayName;
		}

		Component createBadge(String id, IModel<Project> project) {
			return new Label(id, new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return "88";
				}
			});
		}
		
		@Override
		public Component newLink(String id, IModel<Project> project) {
			ProjectCategoryPageLink container = new ProjectCategoryPageLink(id);
			BookmarkablePageLink<Void> link = 
					new BookmarkablePageLink<Void>("link", pageClass, PageSpec.forProject(project.getObject()));
			
			container.add(link);
			WebMarkupContainer icon = new WebMarkupContainer("icon");
			icon.add(AttributeAppender.append("class", Model.of(icon)));
			link.add(icon);
			link.add(new Label("label", Model.of(displayName)));
			link.add(createBadge("badge", project));
			
			return container;
		}
	}

	abstract protected Category getCategory();
	
	public ProjectCategoryPage(PageParameters params) {
		super(params);
	}

	private List<Category> getCategories(ProjectTabGroup group) {
		List<Category> categories = Lists.newArrayList();
		for (Category each : Category.values()) {
			if (each.getGroup() == group) {
				categories.add(each);
			}
		}
		
		return categories;
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		RepeatingView groupView = new RepeatingView("groups");
		add(groupView);
		for (ProjectTabGroup each : ProjectTabGroup.values()) {
			WebMarkupContainer container = new WebMarkupContainer(groupView.newChildId());
			groupView.add(container);
			container.add(new Label("name", Model.of(each.name())));
			container.add(newGroupNavs(each, "nav"));
		}
	}
	
	private Component newGroupNavs(final ProjectTabGroup group, String id) {
		IModel<List<Category>> model = new AbstractReadOnlyModel<List<Category>>() {

			@Override
			public List<Category> getObject() {
				return getCategories(group);
			}
		};
		
		ListView<Category> navs = new ListView<Category>(id, model) {

			@Override
			protected void populateItem(ListItem<Category> item) {
				final Category category = item.getModelObject();
				item.add(category.newLink("link", projectModel));
				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return category == getCategory() ? "active" : "";
					}
					
				}));
			}
		};
		
		return navs;
	}
	
}

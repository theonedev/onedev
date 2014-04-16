package com.pmease.gitop.web.page.repository;

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
import com.pmease.gitop.web.page.repository.api.RepositoryPageTab;
import com.pmease.gitop.web.page.repository.api.RepositoryPageTab.Category;
import com.pmease.gitop.web.page.repository.pullrequest.ClosedRequestsPage;
import com.pmease.gitop.web.page.repository.pullrequest.NewRequestPage;
import com.pmease.gitop.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitop.web.page.repository.pullrequest.RequestDetailPage;
import com.pmease.gitop.web.page.repository.settings.RepositoryOptionsPage;
import com.pmease.gitop.web.page.repository.source.AbstractFilePage;
import com.pmease.gitop.web.page.repository.source.RepositoryHomePage;
import com.pmease.gitop.web.page.repository.source.branches.BranchesPage;
import com.pmease.gitop.web.page.repository.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.repository.source.commits.CommitsPage;
import com.pmease.gitop.web.page.repository.source.contributors.ContributorsPage;
import com.pmease.gitop.web.page.repository.source.tags.TagsPage;
import com.pmease.gitop.web.page.repository.source.tree.SourceTreePage;

@SuppressWarnings("serial")
public abstract class RepositoryTabPage extends RepositoryBasePage {

	protected final IModel<String> revisionModel;
	
	public RepositoryTabPage(PageParameters params) {
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
	protected void onInitialize() {
		super.onInitialize();

		add(createSidebar("sidebar"));
		add(new EmptyRepositoryPanel("nocommit", repositoryModel));
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
				
				setVisibilityAllowed(getRepository().git().hasCommits());
			}
		};
		
		sidebar.add(groups);
		
		AbstractLink adminLink = new BookmarkablePageLink<Void>("settinglink", 
				RepositoryOptionsPage.class, PageSpec.forRepository(getRepository())) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(getRepository())));
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
			rev = getRepository().git().resolveDefaultBranch();
		}
		
		Git git = getRepository().git();
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
	
	private List<RepositoryPageTab> getTabs(final Category group) {
		return ImmutableList.<RepositoryPageTab>copyOf(
				Iterables.filter(getAllTabs(), new Predicate<RepositoryPageTab>() {

			@Override
			public boolean apply(RepositoryPageTab input) {
				return Objects.equal(group.name(), input.getGroupName());
			}
			
		}));
	}
	
	@SuppressWarnings("unchecked")
	private List<RepositoryPageTab> getAllTabs() {
		List<RepositoryPageTab> tabs = Lists.newArrayList();
		
		// SOURCE TABS
		//
		tabs.add(new RepositoryPageTab(Model.of("Code"), 
									Category.SOURCE, 
									"icon-code", 
									new Class[] { SourceTreePage.class, 
												  AbstractFilePage.class }) {
			@Override
			public Class<? extends Page> getBookmarkablePageClass() {
				String rev = SessionData.get().getRevision();
				if (Strings.isNullOrEmpty(rev)) {
					return RepositoryHomePage.class;
				} else {
					return SourceTreePage.class;
				}
			}
		});
		
		tabs.add(new RepositoryPageTab(Model.of("Commits"), 
									Category.SOURCE, 
									"icon-commits", 
									new Class[] { CommitsPage.class, 
												  SourceCommitPage.class }));
		
		tabs.add(new RepositoryPageTab(Model.of("Branches"), 
									Category.SOURCE, 
									"icon-git-branch", 
									BranchesPage.class));
		
		tabs.add(new RepositoryPageTab(Model.of("Tags"), 
									Category.SOURCE, 
									"icon-tags", 
									TagsPage.class));
		
		tabs.add(new RepositoryPageTab(Model.of("Contributors"), 
									Category.SOURCE, 
									"icon-group-o", 
									ContributorsPage.class));
		
		// PULL REQUESTS TABS
		tabs.add(new RepositoryPageTab(Model.of("Open"), 
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
		tabs.add(new RepositoryPageTab(Model.of("Closed"), 
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
		
		tabs.add(new RepositoryPageTab(Model.of("Create"), 
									Category.PULL_REQUESTS, 
									"icon-pull-request", 
									NewRequestPage.class));
		
		return tabs;
	} 

	private Component newGroupNavs(String id, Category group) {
		ListView<RepositoryPageTab> tabs = new ListView<RepositoryPageTab>(id, getTabs(group)) {
			@Override
			protected void populateItem(ListItem<RepositoryPageTab> item) {
				final RepositoryPageTab tab = item.getModelObject();
				
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

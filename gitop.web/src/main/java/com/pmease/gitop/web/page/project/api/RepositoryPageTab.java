package com.pmease.gitop.web.page.project.api;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.SessionData;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.common.wicket.component.tab.AbstractPageTab;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPageLink;

public class RepositoryPageTab extends AbstractPageTab {
	private static final long serialVersionUID = 1L;

	public static enum Category {
		SOURCE,
		PULL_REQUESTS
	}
	
	final Category category;
	final String icon;
	
	public RepositoryPageTab(IModel<String> title, 
			Category category,
			String icon,
			Class<? extends Page>[] pageClasses) {
		super(title, pageClasses);
		this.category = category;
		this.icon = icon;
	}
	
	@SuppressWarnings("unchecked")
	public RepositoryPageTab(IModel<String> title, Category group,
			String icon,
			Class<? extends Page> pageClass) {
		this(title, group, icon, new Class[] { pageClass });
	}
	
	@Override
	public String getGroupName() {
		return category.name();
	}

	public Category getCategory() {
		return category;
	}
	
	public Component newTabLink(String id) {
		ProjectCategoryPageLink container = new ProjectCategoryPageLink(id);
		MarkupContainer link = newPageLink("link");
		container.add(link);
		link.add(new Icon("icon", Model.of(icon)));
		link.add(new Label("label", getTitle()));
		
		Component badge = createBadge("badge", getProject());
		if (badge == null) {
			badge = new WebMarkupContainer("badge");
			badge.setVisibilityAllowed(false);
		}
		
		link.add(badge);
		
		return container;
	}

	protected Repository getProject() {
		return Gitop.getInstance(RepositoryManager.class).get(SessionData.get().getProjectId());
	}
	
	protected MarkupContainer newPageLink(String id) {
		Class<? extends Page> pageClass = getBookmarkablePageClass();
		
		Repository project = getProject();
		PageParameters params = PageSpec.forRepository(project); 
		if (IRevisionAware.class.isAssignableFrom(pageClass)) {
			String revision = SessionData.get().getRevision();
			if (!Strings.isNullOrEmpty(revision)) {
				params.set(PageSpec.OBJECT_ID, revision);
			}
		}
		
		return new BookmarkablePageLink<Void>("link", 
				getBookmarkablePageClass(), 
				params);
	}
	
	@Override
	public final Component newTabLink(String id, PageParameters params) {
		throw new UnsupportedOperationException();
	}
	
	protected Component createBadge(String id, Repository project) {
		return null;
	}
}

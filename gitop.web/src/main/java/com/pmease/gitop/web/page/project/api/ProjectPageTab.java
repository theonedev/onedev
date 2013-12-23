package com.pmease.gitop.web.page.project.api;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.common.wicket.component.tab.AbstractPageTab;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPageLink;

public class ProjectPageTab extends AbstractPageTab {
	private static final long serialVersionUID = 1L;

	public static enum Category {
		SOURCE,
		WIKI,
		ISSUES,
		STATISTICS
	}
	
	final Category category;
	final String icon;
	
	public ProjectPageTab(IModel<String> title, 
			Category category,
			String icon,
			Class<? extends Page>[] pageClasses) {
		super(title, pageClasses);
		this.category = category;
		this.icon = icon;
	}
	
	@SuppressWarnings("unchecked")
	public ProjectPageTab(IModel<String> title, Category group,
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
	
	public Component newTabLink(String id, IModel<Project> projectModel) {
		ProjectCategoryPageLink container = new ProjectCategoryPageLink(id);
		BookmarkablePageLink<Void> link = 
				new BookmarkablePageLink<Void>("link", getBookmarkablePageClass(), 
						PageSpec.forProject(projectModel.getObject()));
		
		container.add(link);
		link.add(new Icon("icon", Model.of(icon)));
		link.add(new Label("label", getTitle()));
		
		Component badge = createBadge("badge", projectModel);
		if (badge == null) {
			badge = new WebMarkupContainer("badge");
			badge.setVisibilityAllowed(false);
		}
		
		link.add(badge);
		
		return container;
	}

	@Override
	public final Component newTabLink(String id, PageParameters params) {
		throw new UnsupportedOperationException();
	}
	
	protected Component createBadge(String id, IModel<Project> project) {
		return null;
	}
}

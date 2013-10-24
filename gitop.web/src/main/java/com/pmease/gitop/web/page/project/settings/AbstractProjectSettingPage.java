package com.pmease.gitop.web.page.project.settings;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.AbstractProjectPage;

@SuppressWarnings("serial")
public abstract class AbstractProjectSettingPage extends AbstractProjectPage {

	public static enum Category {
		OPTIONS("Options"),
		HOOKS("Hooks"),
		MERGE_REQUESTS("Merge Requests"),
		PERMISSIONS("Permissions"),
		AUDIT_LOG("Audit Log");
		
		final String displayName;
		
		Category(final String displayName) {
			this.displayName = displayName;
		}
		
		public String getName() {
			return displayName;
		}
	}
	
	protected abstract Category getCategory();
	
	public AbstractProjectSettingPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(getProject()));
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(createSideNavs("nav"));
	}
	
	@Override
	protected String getPageTitle() {
		return getProject() + " - " + getCategory().getName();
	}

	private Component createSideNavs(String id) {
		ListView<Category> navsView = new ListView<Category>(id, ImmutableList.copyOf(Category.values())) {

			@Override
			protected void populateItem(ListItem<Category> item) {
				Category category = item.getModelObject();
				Component link = newPageLink("link", category);
				item.add(link);
				item.add(AttributeAppender.append("class", category == getCategory() ? "active" : ""));
			}
		};
		
		return navsView;
	}
	
	private Component newPageLink(String id, Category category) {
		Class<? extends Page> pageClass;
		switch (category) {
		case OPTIONS:
			pageClass = ProjectOptionsPage.class;
			break;
			
		case HOOKS:
			pageClass = ProjectHooksPage.class;
			break;
			
		case MERGE_REQUESTS:
			pageClass = MergeRequestSettingsPage.class;
			break;
			
		case AUDIT_LOG:
			pageClass = ProjectAuditLogPage.class;
			break;
			
		case PERMISSIONS:
			pageClass = ProjectPermissionPage.class;
			break;
			
		default:
			throw new IllegalArgumentException();
		}
		
		AbstractLink link = new BookmarkablePageLink<Void>(id, pageClass, PageSpec.forProject(getProject()));
		link.add(new Label("name", category.getName()));
		return link;
	}
}

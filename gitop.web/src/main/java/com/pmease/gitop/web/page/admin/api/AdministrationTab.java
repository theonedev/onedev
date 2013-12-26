package com.pmease.gitop.web.page.admin.api;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.wicket.component.tab.AbstractPageTab;

public class AdministrationTab extends AbstractPageTab  {
	private static final long serialVersionUID = 1L;
	
	public static enum Category {
		ACCOUNTS,
		SETTINGS,
		SUPPORT
	}

	private final Category category;
	
	@SuppressWarnings("unchecked")
	public AdministrationTab(IModel<String> title, 
			Category category,
			Class<? extends WebPage> pageClass) {
		this(title, category, new Class[] { pageClass } );
	}
	
	public AdministrationTab(IModel<String> title, 
			Category category,
			Class<? extends WebPage>[] pageClasses) {
		super(title, pageClasses);
		this.category = category;
	}

	@Override
	public String getGroupName() {
		return category.name();
	}

//	@SuppressWarnings("serial")
//	@Override
//	public Component newTabLink(String id, PageParameters params) {
//		return new LinkPanel(id, getTitle()) {
//
//			@Override
//			protected AbstractLink createLink(String id) {
//				return new BookmarkablePageLink<Void>(id, getBookmarkablePageClass());
//			}
//		};
//	}
//	
//	@Override
//	public String getTabId() {
//		return getTitle().getObject().replace(" ", "-").toLowerCase();
//	}
	
//	@Override
//	public WebMarkupContainer getPanel(String id) {
//		throw new UnsupportedOperationException();
//	}
	
//	@Override
//	public boolean isSelected(Page page) {
//		for (Class<? extends Page> each : pageClasses) {
//			if (each.isAssignableFrom(page.getClass())) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
}

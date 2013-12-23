package com.pmease.gitop.web.page.admin.api;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmease.gitop.web.common.wicket.component.link.LinkPanel;
import com.pmease.gitop.web.common.wicket.component.tab.AbstractPageTab;

public class AdministrationTab extends AbstractPageTab  {
	private static final long serialVersionUID = 1L;
	
	public static enum Category {
		ACCOUNTS,
		SETTINGS,
		SUPPORT
	}

	private final Category category;
	private final Class<? extends WebPage>[] pageClasses;
	
	public AdministrationTab(IModel<String> title, 
			Category category,
			Class<? extends WebPage> pageClass) {
		this(title, category, ImmutableList.<Class<? extends WebPage>>of(pageClass));
	}
	
	@SuppressWarnings("unchecked")
	public AdministrationTab(IModel<String> title, 
			Category category,
			Iterable<Class<? extends WebPage>> pageClasses) {
		this(title, category, Iterables.toArray(pageClasses, Class.class));
	}
	
	public AdministrationTab(IModel<String> title, 
			Category category,
			Class<? extends WebPage>[] pageClasses) {
		super(title, pageClasses);
		
		this.category = category;
		this.pageClasses = pageClasses;
	}

	@Override
	public String getGroupName() {
		return category.name();
	}

	@SuppressWarnings("serial")
	@Override
	public Component newTabLink(String id, PageParameters params) {
		return new LinkPanel(id, getTitle()) {

			@Override
			protected AbstractLink createLink(String id) {
				return new BookmarkablePageLink<Void>(id, pageClasses[0]);
			}
		};
	}
	
	@Override
	public String getTabId() {
		return getTitle().getObject().replace(" ", "-").toLowerCase();
	}
	
	@Override
	public WebMarkupContainer getPanel(String id) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSelected(Page page) {
		for (Class<? extends Page> each : pageClasses) {
			if (each.isAssignableFrom(page.getClass())) {
				return true;
			}
		}
		
		return false;
	}
}

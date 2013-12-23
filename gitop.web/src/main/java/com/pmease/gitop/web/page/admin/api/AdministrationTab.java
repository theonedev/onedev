package com.pmease.gitop.web.page.admin.api;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.parboiled.common.Preconditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmease.gitop.web.common.wicket.component.link.LinkPanel;
import com.pmease.gitop.web.common.wicket.component.tab.AbstractGroupTab;

public class AdministrationTab extends AbstractGroupTab implements IAdministrationTab {
	private static final long serialVersionUID = 1L;
	
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
		super(title);
		this.category = category;
		
		Preconditions.checkArgument(pageClasses != null && pageClasses.length > 0);
		this.pageClasses = pageClasses;
	}

	@Override
	public String getGroupName() {
		return category.name();
	}

	@SuppressWarnings("serial")
	@Override
	public Component newTabLink(String id) {
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

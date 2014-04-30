package com.pmease.commons.wicket.component.tabbable;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

public class PageTab implements Tab {
	
	private static final long serialVersionUID = 1L;

	private final IModel<String> titleModel;
	
	private final List<Class<? extends Page>> pageClasses;
	
	public PageTab(IModel<String> titleModel, Class<? extends Page> pageClass, 
			List<Class<? extends Page>> additionalPageClasses) {
		this.titleModel = titleModel;
		pageClasses = new ArrayList<>();
		pageClasses.add(pageClass);
		pageClasses.addAll(additionalPageClasses);
	}
	
	public PageTab(IModel<String> titleModel, Class<? extends Page> pageClass) {
		this(titleModel, pageClass, new ArrayList<Class<? extends Page>>());
	}

	protected final IModel<String> getTitleModel() {
		return titleModel;
	}
	
	protected final List<Class<? extends Page>> getPageClasses() {
		return pageClasses;
	}
	
	/**
	 * Override this to provide your own logic of populating tab item (the &lt;li&gt; element).
	 * 
	 * @param item
	 * 			The item to populate.
	 * @param componentId
	 * 			Id of the component to add to the item. 
	 */
	@Override
	public void populate(ListItem<Tab> item, String componentId) {
		item.add(new PageTabHeader(componentId, this));
	}

	@Override
	public boolean isActive(ListItem<Tab> item) {
		for (Class<?> pageClass: pageClasses) {
			if (pageClass.isAssignableFrom(item.getPage().getClass())) 
				return true;
		}
		return false;
	}

}

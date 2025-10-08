package io.onedev.server.web.component.tabbable;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.util.WicketUtils;

public class PageTab extends Tab {
	
	private static final long serialVersionUID = 1L;

	private final IModel<String> titleModel;
	
	private final IModel<String> iconModel;
	
	private final Class<? extends Page> mainPageClass;
	
	private final PageParameters mainPageParams;
	
	private final List<Class<? extends Page>> additionalPageClasses;
	
	public PageTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass,
			PageParameters mainPageParams, List<Class<? extends Page>> additionalPageClasses) {
		this.titleModel = titleModel;
		this.iconModel = iconModel;
		this.mainPageClass = mainPageClass;
		this.mainPageParams = mainPageParams;
		this.additionalPageClasses = additionalPageClasses;
	}

	public PageTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, 
				   PageParameters mainPageParams) {
		this(titleModel, iconModel, mainPageClass, mainPageParams, new ArrayList<>());
	}

	public PageTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, 
				   PageParameters mainPageParams, Class<? extends Page> additionalPageClass) {
		this(titleModel, iconModel, mainPageClass, mainPageParams, asList(additionalPageClass));
	}

	public PageTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass,
				   PageParameters mainPageParams, Class<? extends Page> additionalPageClass1, 
				   Class<? extends Page> additionalPageClass2) {
		this(titleModel, iconModel, mainPageClass, mainPageParams, asList(additionalPageClass1, additionalPageClass2));
	}

	public PageTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass,
				   PageParameters mainPageParams, Class<? extends Page> additionalPageClass1, 
				   Class<? extends Page> additionalPageClass2, Class<? extends Page> additionalPageClass3) {
		this(titleModel, iconModel, mainPageClass, mainPageParams, asList(additionalPageClass1, 
				additionalPageClass2, additionalPageClass3));
	}

	public PageTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams,
				   List<Class<? extends Page>> additionalPageClasses) {
		this(titleModel, null, mainPageClass, mainPageParams, additionalPageClasses);
	}

	public PageTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams) {
		this(titleModel, null, mainPageClass, mainPageParams, new ArrayList<>());
	}

	public PageTab(IModel<String> titleModel, Class<? extends Page> mainPageClass,
				   PageParameters mainPageParams, Class<? extends Page> additionalPageClass) {
		this(titleModel, null, mainPageClass, mainPageParams, asList(additionalPageClass));
	}

	public PageTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, 
				   Class<? extends Page> additionalPageClass1, Class<? extends Page> additionalPageClass2) {
		this(titleModel, null, mainPageClass, mainPageParams, asList(additionalPageClass1, additionalPageClass2));
	}

	public PageTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, 
				   Class<? extends Page> additionalPageClass1, Class<? extends Page> additionalPageClass2, 
				   Class<? extends Page> additionalPageClass3) {
		this(titleModel, null, mainPageClass, mainPageParams, asList(additionalPageClass1,
				additionalPageClass2, additionalPageClass3));
	}
	
	private static List<Class<? extends Page>> asList(Class<? extends Page> pageClass) {
		List<Class<? extends Page>> pageClasses = new ArrayList<>();
		pageClasses.add(pageClass);
		return pageClasses;
	}
	
	private static List<Class<? extends Page>> asList(Class<? extends Page> pageClass1, 
			Class<? extends Page> pageClass2) {
		List<Class<? extends Page>> pageClasses = new ArrayList<>();
		pageClasses.add(pageClass1);
		pageClasses.add(pageClass2);
		return pageClasses;
	}

	private static List<Class<? extends Page>> asList(Class<? extends Page> pageClass1, 
			Class<? extends Page> pageClass2, Class<? extends Page> pageClass3) {
		List<Class<? extends Page>> pageClasses = new ArrayList<>();
		pageClasses.add(pageClass1);
		pageClasses.add(pageClass2);
		pageClasses.add(pageClass3);
		return pageClasses;
	}
	
	public final IModel<String> getTitleModel() {
		return titleModel;
	}

	@Nullable
	public final IModel<String> getIconModel() {
		return iconModel;
	}

	public final List<Class<? extends Page>> getAdditionalPageClasses() {
		return additionalPageClasses;
	}
	
	public final Class<? extends Page> getMainPageClass() {
		return mainPageClass;
	}

	public PageParameters getMainPageParams() {
		return mainPageParams;
	}

	/**
	 * Override this to provide your own logic of populating tab item (the &lt;li&gt; element).
	 * 
	 * @param id
	 * 			Id of the component to add to the item. 
	 */
	@Override
	public Component render(String id) {
		return new PageTabHead(id, this);
	}
	
	@Override
	public final boolean isSelected() {
		return isActive(WicketUtils.getPage());
	}

	public boolean isActive(Page currentPage) {
		if (mainPageClass.isAssignableFrom(currentPage.getClass()))
			return true;
		
		for (Class<?> pageClass: additionalPageClasses) {
			if (pageClass.isAssignableFrom(currentPage.getClass())) 
				return true;
		}
		return false;
	}

	@Override
	public String getTitle() {
		return titleModel.getObject();
	}

}

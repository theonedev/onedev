package com.pmease.gitop.web.page.project.settings.gatekeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.loader.ImplementationRegistry;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.gatekeeper.DefaultGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
public abstract class GateKeeperSelector extends Panel {

	public GateKeeperSelector(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final Map<String, List<Class<?>>> gateKeeperClasses = new HashMap<>();
		
		List<Class<?>> implementations = new ArrayList<>();
		for (Class<?> clazz: Gitop.getInstance(ImplementationRegistry.class).getImplementations(GateKeeper.class)) {
			implementations.add(clazz);
		}
		implementations.remove(DefaultGateKeeper.class);
		Collections.sort(implementations, new Comparator<Class<?>>() {

			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return EditableUtils.getOrder(o1) - EditableUtils.getOrder(o2);
			}
			
		});
		
		for (Class<?> implementation: implementations) {
			String category = EditableUtils.getCategory(implementation);
			if (category == null)
				category = GateKeeper.CATEGORY_MISC;
			List<Class<?>> gateKeepersOfCategory = gateKeeperClasses.get(category);
			if (gateKeepersOfCategory == null) {
				gateKeepersOfCategory = new ArrayList<Class<?>>();
				gateKeeperClasses.put(category, gateKeepersOfCategory);
			}
			gateKeepersOfCategory.add(implementation);
		}

		final List<String> categoryOrders = Lists.newArrayList(
				GateKeeper.CATEGORY_COMPOSITE,
				GateKeeper.CATEGORY_BRANCH,
				GateKeeper.CATEGORY_FILE,
				GateKeeper.CATEGORY_BUILD,
				GateKeeper.CATEGORY_APPROVAL,
				GateKeeper.CATEGORY_MISC);
		
		add(new ListView<String>("categories", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> categoryNames = new ArrayList<String>(gateKeeperClasses.keySet());
				Collections.sort(categoryNames, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						int i1 = categoryOrders.indexOf(o1);
						int i2 = categoryOrders.indexOf(o2);
						if (i1 == categoryOrders.size()-1)
							i1 = Integer.MAX_VALUE;
						if (i2 == categoryOrders.size()-1)
							i2 = Integer.MAX_VALUE;
						if (i1 == -1)
							i1 = Integer.MAX_VALUE-1;
						if (i2 == -1)
							i2 = Integer.MAX_VALUE-1;
						if (i1 != i2)
							return i1 - i2;
						else
							return o1.compareTo(o2);
					}
					
				});
				return categoryNames;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> categoryItem) {
				final String category = categoryItem.getModelObject();
				categoryItem.add(new Label("name", category));
				
				List<Class<?>> gateKeepersOfCategory = gateKeeperClasses.get(category);
				Preconditions.checkNotNull(gateKeepersOfCategory);
				
				categoryItem.add(new ListView<Class<?>>("gateKeepers", gateKeepersOfCategory) {

					@Override
					protected void populateItem(ListItem<Class<?>> gateKeeperItem) {
						final Class<?> gateKeeperClass = gateKeeperItem.getModelObject();
						AjaxLink<Void> link = new AjaxLink<Void>("gateKeeper") {

							@SuppressWarnings("unchecked")
							@Override
							public void onClick(AjaxRequestTarget target) {
								onSelect(target, (Class<? extends GateKeeper>) gateKeeperClass);
							}
							
						};
						gateKeeperItem.add(link);
						String icon = EditableUtils.getIcon(gateKeeperClass);
						if (icon == null)
							icon = "icon-lock";
						link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", icon)));
						link.add(new Label("name", EditableUtils.getName(gateKeeperClass)));
						link.add(new Label("description", EditableUtils.getDescription(gateKeeperClass)).setEscapeModelStrings(false));
					}
					
				});
			}
			
		});
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass);
}

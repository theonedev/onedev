package com.pmease.gitplex.web.page.repository.admin.gatekeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.loader.ImplementationRegistry;
import com.pmease.commons.wicket.behavior.CollapseBehavior;
import com.pmease.commons.wicket.component.AccordionPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.DefaultGateKeeper;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
public abstract class GateKeeperSelector extends Panel {

	public GateKeeperSelector(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final Map<String, List<Class<?>>> gateKeeperClasses = new LinkedHashMap<>();
		gateKeeperClasses.put(GateKeeper.CATEGORY_COMMONLY_USED, new ArrayList<Class<?>>());
		gateKeeperClasses.put(GateKeeper.CATEGROY_CHECK_BRANCH, new ArrayList<Class<?>>());
		gateKeeperClasses.put(GateKeeper.CATEGROY_CHECK_FILES, new ArrayList<Class<?>>());
		gateKeeperClasses.put(GateKeeper.CATEGROY_CHECK_REVIEW, new ArrayList<Class<?>>());
		gateKeeperClasses.put(GateKeeper.CATEGROY_CHECK_SUBMITTER, new ArrayList<Class<?>>());
		gateKeeperClasses.put(GateKeeper.CATEGROY_COMPOSITION, new ArrayList<Class<?>>());
		gateKeeperClasses.put(GateKeeper.CATEGROY_OTHERS, new ArrayList<Class<?>>());
		
		List<Class<?>> implementations = new ArrayList<>();
		for (Class<?> clazz: GitPlex.getInstance(ImplementationRegistry.class).getImplementations(GateKeeper.class)) 
			implementations.add(clazz);
		
		implementations.remove(DefaultGateKeeper.class);
		Collections.sort(implementations, new Comparator<Class<?>>() {

			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return EditableUtils.getOrder(o1) - EditableUtils.getOrder(o2);
			}
			
		});
		
		for (Class<?> implementation: implementations) {
			List<Class<?>> categoryClasses = null;
			for (Map.Entry<String, List<Class<?>>> entry: gateKeeperClasses.entrySet()) {
				if (entry.getKey().equals(EditableUtils.getCategory(implementation))) {
					categoryClasses = entry.getValue();
					break;
				}
			}
			if (categoryClasses == null)
				categoryClasses = gateKeeperClasses.get(GateKeeper.CATEGROY_OTHERS);
			categoryClasses.add(implementation);
		}
		
		for (Iterator<Map.Entry<String, List<Class<?>>>> it = gateKeeperClasses.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isEmpty())
				it.remove();
		}
		
		AccordionPanel accordion = new AccordionPanel("accordion");
		add(accordion);
		accordion.add(new ListView<String>("categories", new ArrayList<String>(gateKeeperClasses.keySet())) {

			@Override
			protected void populateItem(ListItem<String> categoryItem) {
				final String category = categoryItem.getModelObject();

				List<Class<?>> gateKeepersOfCategory = gateKeeperClasses.get(category);
				Preconditions.checkNotNull(gateKeepersOfCategory);
				
				WebMarkupContainer collapsible = new WebMarkupContainer("collapsible");
				categoryItem.add(collapsible);
				collapsible.add(new ListView<Class<?>>("gateKeepers", gateKeepersOfCategory) {

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
							icon = "pa-lock";
						link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", icon)));
						link.add(new Label("name", EditableUtils.getName(gateKeeperClass)));
						link.add(new Label("description", EditableUtils.getDescription(gateKeeperClass)).setEscapeModelStrings(false));
					}
					
				});

				WebMarkupContainer collapseTrigger = new WebMarkupContainer("collapseTrigger");
				categoryItem.add(collapseTrigger);
				collapseTrigger.add(new Label("name", category)).add(new CollapseBehavior(collapsible));
			}
			
		});
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass);
}

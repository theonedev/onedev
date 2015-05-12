package com.pmease.commons.wicket.component.tabbable;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class Tabbable extends Panel {
	
	private final List<? extends Tab> tabs;
	
	public Tabbable(String id, List<? extends Tab> tabs) {
		super(id);
		
		this.tabs = tabs;
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		checkComponentTag(openTag, "ul");
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<ActionTab> actionTabs = new ArrayList<>();
		for (Tab tab: tabs) {
			if (tab instanceof ActionTab)
				actionTabs.add((ActionTab) tab);
		}
		if (!actionTabs.isEmpty()) {
			boolean hasSelection = false;
			for (Tab tab: actionTabs) {
				if (tab.isSelected()) {
					hasSelection = true;
					break;
				}
			}
			if (!hasSelection)
				actionTabs.get(0).setSelected(true);
		}
		
		add(new ListView<Tab>("tabs", tabs){

			@Override
			protected void populateItem(ListItem<Tab> item) {
				Tab tab = item.getModelObject();
				tab.setItem(item);
				
				if (tab.isSelected())
					item.add(AttributeModifier.append("class", "active"));

				item.add(tab.render("tab"));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
}

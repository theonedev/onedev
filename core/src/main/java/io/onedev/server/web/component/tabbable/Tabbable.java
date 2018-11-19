package io.onedev.server.web.component.tabbable;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class Tabbable extends Panel {
	
	private static final String OPTIONS_ID = "options";
	
	private final List<Tab> tabs;
	
	public Tabbable(String id, List<? extends Tab> tabs) {
		super(id);
		
		this.tabs = new ArrayList<>();
		for (Tab tab: tabs) {
			this.tabs.add(tab);
		}
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
				if (tab.isSelected())
					item.add(AttributeModifier.append("class", "active"));

				item.add(tab.render("tab"));
			}
			
		});

		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		boolean found = false;
		for (Tab tab: tabs) {
			if (tab.isSelected()) {
				Component options = tab.renderOptions(OPTIONS_ID);
				if (options != null) {
					addOrReplace(options);
					found = true;
				}
				break;
			}
		}
		if (!found)
			addOrReplace(new WebMarkupContainer(OPTIONS_ID).setVisible(false));
		
		super.onBeforeRender();
	}

	public void renderOptions(Tab tab) {
		Component options = tab.renderOptions(OPTIONS_ID);
		if (options != null)
			replace(options);
		else
			replace(new WebMarkupContainer(OPTIONS_ID).setVisible(false));
	}
}

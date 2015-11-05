package com.pmease.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
class AssistPanel extends Panel {

	private final List<AssistItem> assistItems;
	
	private final List<String> recentInputs;
	
	public AssistPanel(String id, List<AssistItem> assistItems, List<String> recentInputs) {
		super(id);
		
		this.assistItems = assistItems;
		this.recentInputs = recentInputs;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<AssistItem>("assistItems", assistItems) {

			@Override
			protected void populateItem(ListItem<AssistItem> item) {
				AssistItem assistItem = item.getModelObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(AttributeAppender.append("data-input", assistItem.getInput()));
				link.add(AttributeAppender.append("data-cursor", assistItem.getCursor()));
				link.add(new Label("label", assistItem.getInput()));
				item.add(link);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!assistItems.isEmpty());
			}
			
		});
		
		add(new ListView<String>("recentInputs", recentInputs) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String recentInput = item.getModelObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new Label("label", recentInput));
				link.add(AttributeAppender.append("data-input", recentInput));
				item.add(link);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!assistItems.isEmpty());
			}
			
		});
		
		setOutputMarkupId(true);
	}

}

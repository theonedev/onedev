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
	
	public AssistPanel(String id, List<AssistItem> assistItems) {
		super(id);
		
		this.assistItems = assistItems;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<AssistItem>("assistItems", assistItems) {

			@Override
			protected void populateItem(ListItem<AssistItem> item) {
				AssistItem assistItem = item.getModelObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new Label("label", assistItem.getInput()));
				item.add(link);
				item.add(AttributeAppender.append("data-input", assistItem.getInput()));
				item.add(AttributeAppender.append("data-cursor", assistItem.getCursor()));
			}

		});
		
		setOutputMarkupId(true);
	}

}

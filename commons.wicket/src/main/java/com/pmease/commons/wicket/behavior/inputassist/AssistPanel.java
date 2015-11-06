package com.pmease.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.CssResourceReference;

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
				link.add(new Label("label", assistItem.getInput()));
				item.add(link);
				item.add(AttributeAppender.append("data-input", assistItem.getInput()));
				item.add(AttributeAppender.append("data-cursor", assistItem.getCursor()));
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
				item.add(link);
				item.add(AttributeAppender.append("data-input", recentInput));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!recentInputs.isEmpty());
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(AssistPanel.class, "input-assist.css")));
	}

}

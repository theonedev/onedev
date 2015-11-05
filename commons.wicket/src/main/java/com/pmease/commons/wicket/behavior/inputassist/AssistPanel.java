package com.pmease.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class AssistPanel extends Panel {

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
				final AssistItem assistItem = item.getModelObject();
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, assistItem);
					}
					
				};
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
				final String recentInput = item.getModelObject();
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, recentInput);
					}
					
				};
				link.add(new Label("label", recentInput));
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

	protected abstract void onSelect(AjaxRequestTarget target, AssistItem assistItem);
	
	protected abstract void onSelect(AjaxRequestTarget target, String recentInput);
	
}

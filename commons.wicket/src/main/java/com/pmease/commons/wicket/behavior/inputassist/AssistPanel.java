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

	private final List<InputAssist> assists;
	
	public AssistPanel(String id, List<InputAssist> assists) {
		super(id);
		
		this.assists = assists;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<InputAssist>("assists", assists) {

			@Override
			protected void populateItem(ListItem<InputAssist> item) {
				InputAssist assist = item.getModelObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new Label("label", assist.getInput()));
				item.add(link);
				item.add(AttributeAppender.append("data-input", assist.getInput()));
				item.add(AttributeAppender.append("data-caret", assist.getCaret()));
			}

		});
		
		setOutputMarkupId(true);
	}

}

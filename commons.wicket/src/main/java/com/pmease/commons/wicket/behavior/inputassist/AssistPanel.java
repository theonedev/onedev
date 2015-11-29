package com.pmease.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.antlr.codeassist.InputSuggestion;

@SuppressWarnings("serial")
class AssistPanel extends Panel {

	private final List<InputSuggestion> suggestions;
	
	public AssistPanel(String id, List<InputSuggestion> suggestions) {
		super(id);
		this.suggestions = suggestions;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<InputSuggestion>("suggestions", suggestions) {

			@Override
			protected void populateItem(ListItem<InputSuggestion> item) {
				InputSuggestion suggestion = item.getModelObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new Label("label", suggestion.getLabel()));
				item.add(link);
				if (suggestion.getDescription() != null)
					item.add(new Label("description", suggestion.getDescription()));
				else
					item.add(new Label("description").setVisible(false));
				item.add(AttributeAppender.append("data-content", suggestion.getContent()));
				item.add(AttributeAppender.append("data-caret", suggestion.getCaret()));
			}

		});

		setOutputMarkupId(true);
	}

}

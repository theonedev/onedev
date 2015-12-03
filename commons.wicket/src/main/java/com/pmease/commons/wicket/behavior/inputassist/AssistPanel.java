package com.pmease.commons.wicket.behavior.inputassist;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;

@SuppressWarnings("serial")
class AssistPanel extends Panel {

	private final InputStatus inputStatus;
	
	private final List<InputCompletion> suggestions;
	
	public AssistPanel(String id, InputStatus inputStatus, List<InputCompletion> suggestions) {
		super(id);
		this.inputStatus = inputStatus;
		this.suggestions = suggestions;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<InputCompletion>("suggestions", suggestions) {

			@Override
			protected void populateItem(ListItem<InputCompletion> item) {
				InputCompletion suggestion = item.getModelObject();
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new Label("label", suggestion.getReplaceContent()));
				item.add(link);
				if (suggestion.getDescription() != null)
					item.add(new Label("description", suggestion.getDescription()));
				else
					item.add(new Label("description").setVisible(false));
				item.add(AttributeAppender.append("data-content", suggestion.complete(inputStatus).getContent()));
				item.add(AttributeAppender.append("data-caret", suggestion.getCaret()));
			}

		});

		setOutputMarkupId(true);
	}

}

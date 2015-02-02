package com.pmease.commons.wicket.component.markdown;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.behavior.markdown.MarkdownBehavior;

@SuppressWarnings("serial")
public class MarkdownInput extends TextArea<String> {

	public MarkdownInput(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new MarkdownBehavior());
	}

}

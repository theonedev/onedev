package com.pmease.gitplex.web.component.markdown;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class MarkdownInput extends FormComponentPanel<String> {

	private TextArea<String> input;
	
	public MarkdownInput(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new TextArea<String>("input", getModel()));
		input.add(new MarkdownBehavior());
	}

	@Override
	protected void convertInput() {
		setConvertedInput(input.getConvertedInput());
	}

}

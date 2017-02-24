package com.gitplex.server.web.page.depot.blob.render.renderers.source;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

import com.google.common.base.Charsets;

@SuppressWarnings("serial")
class SourceFormComponent extends FormComponentPanel<byte[]> {

	private TextArea<String> input;
	
	public SourceFormComponent(String id, byte[] initialContent) {
		super(id, Model.of(initialContent));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new TextArea<String>("input", Model.of(new String(getModelObject(), Charsets.UTF_8))));
		setOutputMarkupId(true);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null)
			setConvertedInput(content.getBytes());
		else
			setConvertedInput(new byte[0]);
	}
	
}

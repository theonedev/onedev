package com.gitplex.server.web.page.depot.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

import com.gitplex.server.web.behavior.markdown.MarkdownBehavior;
import com.google.common.base.Charsets;

@SuppressWarnings("serial")
public class MarkdownFormComponent extends FormComponentPanel<byte[]> {

	private final boolean autoFocus;
	
	private TextArea<String> input;
	
	public MarkdownFormComponent(String id, byte[] initialContent, boolean autoFocus) {
		super(id, Model.of(initialContent));
		this.autoFocus = autoFocus;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new TextArea<String>("input", Model.of(new String(getModelObject(), Charsets.UTF_8)));
		
		input.add(new MarkdownBehavior());
		add(input);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null)
			setConvertedInput(content.getBytes());
		else
			setConvertedInput(new byte[0]);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (autoFocus) {
			String script = String.format("$('#%s').focus();", input.getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

}

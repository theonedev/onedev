package io.onedev.server.web.page.project.blob.render.edit.plain;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class PlainEditPanel extends FormComponentPanel<String> {
	
	private final String fileName;
	
	private TextArea<String> input;
	
	public PlainEditPanel(String id, String fileName, String initialContent) {
		super(id, Model.of(initialContent));
		this.fileName = fileName;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new TextArea<String>("input", Model.of(getModelObject())) {

			@Override
			protected boolean shouldTrimInput() {
				return false;
			}
			
		});
	}

	@Override
	public void convertInput() {
		if (input.getConvertedInput() != null)
			setConvertedInput(input.getConvertedInput());
		else
			setConvertedInput("");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new PlainEditResourceReference()));
		
		String script = String.format("onedev.server.plainEdit.onDomReady('%s', '%s');", 
				getMarkupId(), fileName);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}

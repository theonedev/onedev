package io.onedev.server.web.editable.script;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class ScriptPropertyViewer extends Panel {

	private final String script;
	
	private TextArea<String> input;
	
	public ScriptPropertyViewer(String id, String script) {
		super(id);
		
		this.script = script;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new TextArea<>("input", Model.of(script));
		add(input);
		
		input.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ScriptSupportResourceReference()));
		
		String script = String.format("onedev.server.scriptSupport.onViewerDomReady('%s');", input.getMarkupId());
		response.render(OnLoadHeaderItem.forScript(script));
	}

}

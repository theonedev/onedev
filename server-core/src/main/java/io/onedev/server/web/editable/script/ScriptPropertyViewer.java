package io.onedev.server.web.editable.script;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.commons.utils.StringUtils;

@SuppressWarnings("serial")
public class ScriptPropertyViewer extends Panel {

	private final List<String> script;
	
	private final String modeName;
	
	private TextArea<String> input;
	
	public ScriptPropertyViewer(String id, List<String> script, String modeName) {
		super(id);
		
		this.modeName = modeName;
		this.script = script;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new TextArea<>("input", Model.of(StringUtils.join(script, "\n")));
		add(input);
		
		input.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ScriptSupportResourceReference()));
		
		String script = String.format("onedev.server.scriptSupport.onViewerDomReady('%s', '%s');", 
				input.getMarkupId(), modeName);
		response.render(OnLoadHeaderItem.forScript(script));
	}

}

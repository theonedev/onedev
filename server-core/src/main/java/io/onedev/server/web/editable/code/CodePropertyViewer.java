package io.onedev.server.web.editable.code;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.commons.utils.StringUtils;

@SuppressWarnings("serial")
public class CodePropertyViewer extends Panel {

	private final List<String> code;
	
	private final String language;
	
	private TextArea<String> input;
	
	public CodePropertyViewer(String id, List<String> code, String language) {
		super(id);
		
		this.language = language;
		this.code = code;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new TextArea<>("input", Model.of(StringUtils.join(code, "\n")));
		add(input);
		
		input.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CodeSupportResourceReference()));
		
		String script = String.format("onedev.server.codeSupport.onViewerDomReady('%s', '%s');", 
				input.getMarkupId(), language);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}

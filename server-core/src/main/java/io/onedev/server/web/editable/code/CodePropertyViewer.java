package io.onedev.server.web.editable.code;

import io.onedev.commons.utils.StringUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class CodePropertyViewer extends Panel {

	private final Serializable code;
	
	private final String language;
	
	private TextArea<String> input;
	
	public CodePropertyViewer(String id, Serializable code, String language) {
		super(id);
		
		this.language = language;
		this.code = code;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (code instanceof List)
			input = new TextArea<>("input", Model.of(StringUtils.join((List<?>)code, "\n")));
		else
			input = new TextArea<>("input", Model.of((String)code));
			
		add(input);
		
		input.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CodeSupportResourceReference()));
		
		String script = String.format("onedev.server.codeSupport.onViewerLoad('%s', '%s');", 
				input.getMarkupId(), language);
		
		// Initialize codemirror via onLoad; otherwise, it will not be shown 
		// correctly in a modal dialog
		response.render(OnLoadHeaderItem.forScript(script));
	}

}

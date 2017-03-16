package com.gitplex.server.web.component.markdown;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.javascript.JavaScriptEscape;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;

@SuppressWarnings("serial")
public class MarkdownEditor extends FormComponentPanel<String> {

	private final boolean compactMode;
	
	private TextArea<String> input;

	private AbstractPostAjaxBehavior ajaxBehavior;
	
	/**
	 * @param id 
	 * 			component id of the editor
	 * @param model
	 * 			markdown model of the editor
	 * @param compactMode
	 * 			editor in compact mode occupies horizontal space and is suitable 
	 * 			to be used in places such as comment aside the code
	 */
	public MarkdownEditor(String id, IModel<String> model, boolean compactMode) {
		super(id, model);
		this.compactMode = compactMode;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer splitIcon = new WebMarkupContainer("splitIcon");
		add(splitIcon);
		if (compactMode)
			splitIcon.add(AttributeAppender.append("class", "fa-rotate-270"));

		WebMarkupContainer body = new WebMarkupContainer("body");
		if (compactMode)
			body.add(AttributeAppender.append("class", "compact"));
			
		add(body);
		
		body.add(input = new TextArea<String>("input", Model.of(getModelObject())));
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setChannel(new AjaxChannel("markdown-preview", AjaxChannel.Type.DROP));
			}

			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				switch (params.getParameterValue("action").toString()) {
				case "render":
					String markdown = params.getParameterValue("param1").toString();
					String rendered;
					if (StringUtils.isNotBlank(markdown)) {
						MarkdownManager markdownManager = GitPlex.getInstance(MarkdownManager.class);
						rendered = markdownManager.render(markdown, true);
					} else {
						rendered = "<div class='message'>Nothing to preview</div>";
					}
					String script = String.format("gitplex.server.markdown.onRendered('%s', '%s');", 
							getMarkupId(), JavaScriptEscape.escapeJavaScript(rendered));
					target.appendJavaScript(script);
					break;
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void convertInput() {
		setConvertedInput(input.getConvertedInput());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		
		String callback = ajaxBehavior.getCallbackFunction(explicit("action"), explicit("param1"), explicit("param2"), 
				explicit("param3"), explicit("param4"), explicit("param5")).toString();
		
		String script = String.format("gitplex.server.markdown.onDomReady('%s', %s);", getMarkupId(), callback);
		response.render(OnDomReadyHeaderItem.forScript(script));
		
		script = String.format("gitplex.server.markdown.onWindowLoad('%s');", getMarkupId());
		response.render(OnLoadHeaderItem.forScript(script));
	}

}

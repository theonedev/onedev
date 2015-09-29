package com.pmease.commons.wicket.component.markdownviewer;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import static org.apache.wicket.ajax.attributes.CallbackParameter.*;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.base.Preconditions;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.markdown.MarkdownManager;

@SuppressWarnings("serial")
public class MarkdownViewer extends GenericPanel<String> {

	private final boolean taskEditable;
	
	public MarkdownViewer(String id, IModel<String> model, boolean taskEditable) {
		super(id, model);
		
		this.taskEditable = taskEditable;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("html", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return AppLoader.getInstance(MarkdownManager.class)
						.parseAndProcess(MarkdownViewer.this.getModelObject());
			}
		}).setEscapeModelStrings(false));

		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				Preconditions.checkState(taskEditable);
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String taskStartIndex = params.getParameterValue("taskStartIndex").toString();
				String taskEndIndex = params.getParameterValue("taskEndIndex").toString();
				boolean taskChecked = params.getParameterValue("taskChecked").toBoolean();
				
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(MarkdownViewerResourceReference.INSTANCE));
				
				CharSequence callbackFunc = getCallbackFunction(
						explicit("taskStartIndex"), 
						explicit("taskEndIndex"), 
						explicit("taskChecked"));
				String script = String.format("pmease.commons.initMarkdownViewer($('#%s>.md-preview'), %s);", 
						getMarkupId(true), taskEditable?callbackFunc:"undefined");
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
	}

}

package com.pmease.gitplex.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;

@SuppressWarnings("serial")
abstract class ConfirmDeletePanel extends Panel {
	
	public ConfirmDeletePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("message", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getWarningMessage();
			}
			
		}));
		
		add(new PreventDefaultAjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(ConfirmDeletePanel.class, "confirm-delete.css")));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(ConfirmDeletePanel.class, "confirm-delete.js")));
		
		String script = String.format("gitplex.confirmDelete('%s', '%s');", getMarkupId(), getConfirmInput());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract String getWarningMessage();
	
	protected abstract String getConfirmInput();
	
	protected abstract void onDelete(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}

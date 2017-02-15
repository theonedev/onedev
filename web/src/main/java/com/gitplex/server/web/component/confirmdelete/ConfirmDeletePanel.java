package com.gitplex.server.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.gitplex.server.web.component.PreventDefaultAjaxLink;

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
		response.render(JavaScriptHeaderItem.forReference(new ConfirmDeleteResourceReference()));
		
		String script = String.format("gitplex.server.confirmDelete('%s', '%s');", getMarkupId(), getConfirmInput());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract String getWarningMessage();
	
	protected abstract String getConfirmInput();
	
	protected abstract void onDelete(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}

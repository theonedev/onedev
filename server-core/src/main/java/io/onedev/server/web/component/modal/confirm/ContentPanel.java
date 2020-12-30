package io.onedev.server.web.component.modal.confirm;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.web.component.link.PreventDefaultAjaxLink;

@SuppressWarnings("serial")
abstract class ContentPanel extends Panel {
	
	public ContentPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(new Label("message", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getConfirmMessage();
			}
			
		}));

		add(new FencedFeedbackPanel("feedback", this).setEscapeModelStrings(false));
		
		add(new WebMarkupContainer("input").setVisible(getConfirmInput() != null));
		
		add(new PreventDefaultAjaxLink<Void>("ok") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					onConfirm(target);
				} catch (ExplicitException e) {
					error(e.getMessage());
					target.add(ContentPanel.this);
				}
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
		response.render(JavaScriptHeaderItem.forReference(new ConfirmResourceReference()));
		
		String script = String.format("onedev.server.confirm('%s', %s);", 
				getMarkupId(), getConfirmInput()!=null?"'" + JavaScriptEscape.escapeJavaScript(getConfirmInput()) + "'": "undefined");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract String getConfirmMessage();

	@Nullable
	protected abstract String getConfirmInput();
	
	protected abstract void onConfirm(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}

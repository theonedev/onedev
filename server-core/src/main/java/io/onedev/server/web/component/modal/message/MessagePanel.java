package io.onedev.server.web.component.modal.message;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.PreventDefaultAjaxLink;

@SuppressWarnings("serial")
abstract class MessagePanel extends Panel {
	
	public MessagePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				close(target);
			}
			
		});
		
		add(newMessageContent("content"));

		add(new PreventDefaultAjaxLink<Void>("ok") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				close(target);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MessageCssResourceReference()));
	}

	protected abstract Component newMessageContent(String componentId);

	protected abstract void close(AjaxRequestTarget target);
}

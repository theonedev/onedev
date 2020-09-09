package io.onedev.server.web.component.sideinfo;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.asset.jqueryui.JQueryUIResourceReference;

@SuppressWarnings("serial")
public abstract class SideInfoPanel extends Panel {

	public SideInfoPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newTitle("title"));
		add(newBody("body"));

		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript(String.format("$('#%s').addClass('closed');", SideInfoPanel.this.getMarkupId()));
			}
			
		});
		
		add(AttributeAppender.append("class", "side-info d-flex flex-column closed"));
		
		setOutputMarkupId(true);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof SideInfoOpened) {
			SideInfoOpened sideInfoOpened = (SideInfoOpened) event.getPayload();
			sideInfoOpened.getHandler().appendJavaScript(String.format(""
					+ "$('#%s').removeClass('closed'); "
					+ "setTimeout(function(){$(window).resize();}, 350);", // Wait for animation to stop 
					getMarkupId()));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		response.render(CssHeaderItem.forReference(new SideInfoCssResourceReference()));
	}

	protected abstract Component newTitle(String componentId);
	
	protected abstract Component newBody(String componentId);
	
}

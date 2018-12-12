package io.onedev.server.web.component.moreinfoside;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public abstract class MoreInfoSidePanel extends Panel {

	public MoreInfoSidePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent("content"));
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				String script = String.format("$('#%s').toggleClass('closed');", MoreInfoSidePanel.this.getMarkupId());
				target.appendJavaScript(script);
				send(getPage(), Broadcast.BREADTH, new MoreInfoSideClosed(target));
			}
			
		});
		
		add(AttributeAppender.append("class", "more-info-side closed"));
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof MoreInfoSideOpened) {
			MoreInfoSideOpened moreInfoSideOpened = (MoreInfoSideOpened) event.getPayload();
			String script = String.format("$('#%s').toggleClass('closed');", getMarkupId());
			moreInfoSideOpened.getHandler().appendJavaScript(script);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new MoreInfoSideResourceReference()));
		
		String script = String.format("onedev.server.moreInfoSide.onDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String componentId);
	
}

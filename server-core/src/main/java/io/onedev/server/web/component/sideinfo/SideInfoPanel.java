package io.onedev.server.web.component.sideinfo;

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
public abstract class SideInfoPanel extends Panel {

	public SideInfoPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent("content"));
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				String script = String.format("$('#%s').hide('slide', {direction: 'right', duration: 200});", SideInfoPanel.this.getMarkupId());
				target.appendJavaScript(script);
				send(getPage(), Broadcast.BREADTH, new SideInfoClosed(target));
			}
			
		});
		
		add(AttributeAppender.append("class", "side-info closed"));
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof SideInfoOpened) {
			SideInfoOpened moreInfoSideOpened = (SideInfoOpened) event.getPayload();
			String script = String.format(""
					+ "$('#%s').show('slide', {"
					+ "  direction: 'right', "
					+ "  duration: 200, "
					+ "  complete: function() {"
					+ "    if (!onedev.server.util.isDevice()) "
					+ "      $(this).data('ps').update();"
					+ "  }"
					+ "});", getMarkupId());
			moreInfoSideOpened.getHandler().appendJavaScript(script);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new SideInfoResourceReference()));
		
		String script = String.format("onedev.server.sideInfo.onDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String componentId);
	
}

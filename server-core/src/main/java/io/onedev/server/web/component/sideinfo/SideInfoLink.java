package io.onedev.server.web.component.sideinfo;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;

@SuppressWarnings("serial")
public class SideInfoLink extends AjaxLink<Void> {

	public SideInfoLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof SideInfoClosed) {
			SideInfoClosed moreInfoSideClosed = (SideInfoClosed) event.getPayload();
			moreInfoSideClosed.getHandler().appendJavaScript(
					String.format("$('#%s').show();", getMarkupId()));
		}
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("$('#%s').hide();", getMarkupId()));
		send(getPage(), Broadcast.BREADTH, new SideInfoOpened(target));
	}

}

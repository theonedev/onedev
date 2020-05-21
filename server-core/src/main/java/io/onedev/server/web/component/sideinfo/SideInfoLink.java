package io.onedev.server.web.component.sideinfo;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;

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
	public void onClick(AjaxRequestTarget target) {
		send(getPage(), Broadcast.BREADTH, new SideInfoOpened(target));
	}

}

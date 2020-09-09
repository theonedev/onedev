package io.onedev.server.web.component.sideinfo;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

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

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SideInfoCssResourceReference()));
	}

}

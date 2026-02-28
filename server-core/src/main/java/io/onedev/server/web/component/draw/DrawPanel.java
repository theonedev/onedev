package io.onedev.server.web.component.draw;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.util.Animation;

public abstract class DrawPanel extends FloatingPanel {

	public enum Placement {LEFT, RIGHT}
	
	public DrawPanel(AjaxRequestTarget target, Placement placement) {
		super(target, null, false, false, Animation.valueOf(placement.name()));
		add(AttributeAppender.append("class", "draw draw-" + placement.name().toLowerCase()));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new DrawCssResourceReference()));
	}
	
}

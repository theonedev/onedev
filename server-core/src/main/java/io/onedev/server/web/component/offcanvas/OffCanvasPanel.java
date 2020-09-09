package io.onedev.server.web.component.offcanvas;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.util.Animation;

@SuppressWarnings("serial")
public abstract class OffCanvasPanel extends FloatingPanel {

	public enum Placement {LEFT, RIGHT}
	
	public OffCanvasPanel(AjaxRequestTarget target, Placement placement, @Nullable String width) {
		super(target, null, Animation.valueOf(placement.name()));
		add(AttributeAppender.append("class", "off-canvas off-canvas-" + placement.name().toLowerCase()));
		if (width != null)
			add(AttributeAppender.append("style", "width: " + width).setSeparator(";"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new OffCanvasCssResourceReference()));
	}
	
}

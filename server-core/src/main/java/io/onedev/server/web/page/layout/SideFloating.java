package io.onedev.server.web.page.layout;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;

import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.util.Animation;

@SuppressWarnings("serial")
public abstract class SideFloating extends FloatingPanel {

	public enum Placement {LEFT, RIGHT}
	
	public SideFloating(AjaxRequestTarget target, Placement placement) {
		super(target, null, Animation.valueOf(placement.name()));
		add(AttributeAppender.append("class", "side side-" + placement.name().toLowerCase()));
	}

	protected abstract String getTitle();
	
	protected Component renderSubHeader(String componentId) {
		return new WebMarkupContainer(componentId).setVisible(false);
	}
	
	protected abstract Component newBody(String id);
	
	@Override
	protected Component newContent(String id) {
		return new CloseablePanel(id, getTitle()) {

			protected void onClose(AjaxRequestTarget target) {
				close();
			}

			@Override
			protected Component renderSubHeader(String componentId) {
				return SideFloating.this.renderSubHeader(componentId);
			}

			@Override
			protected Component newFloatingContent(String id) {
				return SideFloating.this.newBody(id);
			}
			
		};
	}
	
}

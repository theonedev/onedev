package io.onedev.server.web.component.draw;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.jspecify.annotations.Nullable;

public abstract class DrawCardPanel extends DrawPanel {

	public DrawCardPanel(AjaxRequestTarget target, Placement placement, @Nullable String width) {
		super(target, placement, width);
		add(AttributeAppender.append("class", "draw-card"));
	}

	@Override
	protected Component newContent(String id) {
		return new DrawCardSupportPanel(id) {
			
			@Override
			protected void onClose(AjaxRequestTarget target) {
				close();
			}
			
			@Override
			protected Component newTitle(String componentId) {
				return DrawCardPanel.this.newTitle(componentId);
			}
			
			@Override
			protected Component newBody(String componentId) {
				return DrawCardPanel.this.newBody(componentId);
			}

			@Override
			protected Component newFooter(String componentId) {
				return DrawCardPanel.this.newFooter(componentId);
			}
			
		};
	}
	
	protected abstract Component newTitle(String componentId);

	protected abstract Component newBody(String componentId);
	
	@Nullable
	protected Component newFooter(String componentId) {
		return null;
	}
	
}

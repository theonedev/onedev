package io.onedev.server.web.component.offcanvas;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;

@SuppressWarnings("serial")
public abstract class OffCanvasCardPanel extends OffCanvasPanel {

	public OffCanvasCardPanel(AjaxRequestTarget target, Placement placement, @Nullable String width) {
		super(target, placement, width);
		add(AttributeAppender.append("class", "off-canvas-card"));
	}

	@Override
	protected Component newContent(String id) {
		return new OffCanvasCardSupportPanel(id) {
			
			@Override
			protected void onClose(AjaxRequestTarget target) {
				close();
			}
			
			@Override
			protected Component newTitle(String componentId) {
				return OffCanvasCardPanel.this.newTitle(componentId);
			}
			
			@Override
			protected Component newBody(String componentId) {
				return OffCanvasCardPanel.this.newBody(componentId);
			}

			@Override
			protected Component newFooter(String componentId) {
				return OffCanvasCardPanel.this.newFooter(componentId);
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

package io.onedev.server.web.editable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public abstract class PropertyViewer extends Panel {

	private final PropertyDescriptor descriptor;
	
	public PropertyViewer(String id, PropertyDescriptor descriptor) {
		super(id);
	
		this.descriptor = descriptor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newContent("content", descriptor));
		add(AttributeAppender.append("class", "property-viewer editable"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new EditableResourceReference()));
	}
	
	protected abstract Component newContent(String id, PropertyDescriptor descriptor);
}

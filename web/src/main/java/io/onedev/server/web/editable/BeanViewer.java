package io.onedev.server.web.editable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public abstract class BeanViewer extends Panel {

	private final BeanDescriptor beanDescriptor;
	
	public BeanViewer(String id, BeanDescriptor beanDescriptor) {
		super(id);
		this.beanDescriptor = beanDescriptor;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent("content", beanDescriptor));
		add(AttributeAppender.append("class", "bean viewer editable"));
	}

	protected abstract Component newContent(String id, BeanDescriptor beanDescriptor);
}

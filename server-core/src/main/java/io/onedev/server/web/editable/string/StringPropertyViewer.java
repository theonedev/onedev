package io.onedev.server.web.editable.string;

import java.lang.reflect.AnnotatedElement;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyViewer;

public class StringPropertyViewer extends PropertyViewer {

	private final String value;
	
	public StringPropertyViewer(String id, PropertyDescriptor descriptor, String value) {
		super(id, descriptor);
		this.value = value;
	}

	@Override
	protected Component newContent(String id, PropertyDescriptor descriptor) {
		if (value != null) {
			return new Label(id, "<pre class='mb-0'>" + HtmlEscape.escapeHtml5(value) + "</pre>").setEscapeModelStrings(false);
		} else { 
			return new EmptyValueLabel(id) {

				@Override
				protected AnnotatedElement getElement() {
					return descriptor.getPropertyGetter();
				}
				
			};
		}
	}

}

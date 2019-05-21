package io.onedev.server.web.editable.string;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class StringPropertyViewer extends PropertyViewer {

	private final String value;
	
	public StringPropertyViewer(String id, PropertyDescriptor descriptor, String value) {
		super(id, descriptor);
		this.value = value;
	}

	@Override
	protected Component newContent(String id, PropertyDescriptor descriptor) {
		if (value != null) 
			return new Label(id, HtmlUtils.formatAsHtml(value)).setEscapeModelStrings(false);
		else 
			return new EmptyValueLabel(id, descriptor.getPropertyGetter());
	}

}

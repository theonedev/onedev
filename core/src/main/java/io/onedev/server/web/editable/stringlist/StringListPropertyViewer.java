package io.onedev.server.web.editable.stringlist;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@SuppressWarnings("serial")
public class StringListPropertyViewer extends PropertyViewer {

	private final List<String> value;
	
	public StringListPropertyViewer(String id, PropertyDescriptor descriptor, List<String> value) {
		super(id, descriptor);
		this.value = value;
	}

	@Override
	protected Component newContent(String id, PropertyDescriptor descriptor) {
        if (value != null && !value.isEmpty()) {
        	return new MultilineLabel(id, StringUtils.join(value, "\n"));
        } else {
			NameOfEmptyValue nameOfEmptyValue = descriptor.getPropertyGetter().getAnnotation(NameOfEmptyValue.class);
			if (nameOfEmptyValue != null)
				return new Label(id, nameOfEmptyValue.value());
			else 
				return new EmptyValueLabel(id, descriptor.getPropertyGetter());
        }
	}

}

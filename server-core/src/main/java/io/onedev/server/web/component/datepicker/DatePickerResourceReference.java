package io.onedev.server.web.component.datepicker;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class DatePickerResourceReference extends BaseDependentResourceReference {
	
	private static final long serialVersionUID = 1L;
	
	public DatePickerResourceReference() {
		super(DatePickerResourceReference.class, "date-picker.js");
	}
	
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(DatePickerResourceReference.class, "flatpickr.min.js")
		));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(DatePickerResourceReference.class, "flatpickr.min.css")
		));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(DatePickerResourceReference.class, "flatpickr-locale.js")
		));
		
		return dependencies;
	}
}

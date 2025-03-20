package io.onedev.server.web.component.datepicker;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.flatpickr.FlatPickrResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class DatePickerResourceReference extends BaseDependentResourceReference {
	
	private static final long serialVersionUID = 1L;
	
	public DatePickerResourceReference() {
		super(DatePickerResourceReference.class, "date-picker.js");
	}
	
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();		
		dependencies.add(JavaScriptHeaderItem.forReference(new FlatPickrResourceReference()));
		return dependencies;
	}
}

package io.onedev.server.web.editable.stringlist;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class StringListEditor extends PropertyEditor<List<String>> {

	private Select2MultiChoice<String> input;
	
	public StringListEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<String> selections = getModelObject();
		
		if (selections == null) 
			selections = new ArrayList<>();
		input = new StringMultiChoice("input", Model.of(selections), Model.ofList(new ArrayList<>()), true) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setContainerCssClass("string-list");
				getSettings().setDropdownCssClass("string-list");
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
        add(input);
    }

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null)
			return new ArrayList<>(convertedInput);
		else
			return new ArrayList<>();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}

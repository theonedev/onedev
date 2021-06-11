package io.onedev.server.web.editable.emaillist;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Splitter;

import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings({"serial", "unchecked", "rawtypes"})
public class EmailListPropertyEditor extends PropertyEditor<List<String>> {

	private TextArea<String> input;
	
	public EmailListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String emailListString;
		
		List<String> emailList = getModelObject();
		if (emailList != null)
			emailListString = StringUtils.join(emailList, "\n");
		else
			emailListString = "";
		
        input = new TextArea("input", Model.of(emailListString));
        
        input.setRequired(descriptor.isPropertyRequired());
        input.setLabel(Model.of(getDescriptor().getDisplayName()));

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
		List<String> values = new ArrayList<>();
		if (input.getConvertedInput() != null) {
			values.addAll(Splitter
					.on('\n')
					.trimResults()
					.omitEmptyStrings()
					.splitToList(input.getConvertedInput()));
		}
        return values;
	}

}

package io.onedev.server.web.editable.image;

import io.onedev.server.annotation.Image;
import io.onedev.server.web.component.imagedata.upload.ImageUploadField;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

public class ImagePropertyEditor extends PropertyEditor<String> {

	private ImageUploadField input;
	
	public ImagePropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var image = descriptor.getPropertyGetter().getAnnotation(Image.class);
		input = new ImageUploadField("input", Model.of(getModelObject()), 
				image.accept(), image.width(), image.height(), image.backgroundColor()) {
			@Override
			protected void onImageUpdating(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
		};
		add(input);
		input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}

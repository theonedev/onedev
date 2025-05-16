package io.onedev.server.web.editable.markdown;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.markdown.MarkdownEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class MarkdownPropertyEditor extends PropertyEditor<String> {

	private MarkdownEditor input;
	
	public MarkdownPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new MarkdownEditor("input", Model.of(getModelObject()), false, null) {

			@Override
			protected List<Behavior> getInputBehaviors() {
				List<Behavior> behaviors = MarkdownPropertyEditor.this.getInputBehaviors();
				behaviors.add(new OnTypingDoneBehavior() {

					@Override
					protected void onTypingDone(AjaxRequestTarget target) {
						onPropertyUpdating(target);
					}
					
				});
				return behaviors;
			}
			
		});
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
	}

	protected List<Behavior> getInputBehaviors() {
		return new ArrayList<>();
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}

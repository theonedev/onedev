package io.onedev.server.web.editable;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.util.ComponentContext;

@SuppressWarnings("serial")
public abstract class PropertyEditor<T> extends ValueEditor<T> {

	protected final PropertyDescriptor descriptor;
	
	public PropertyEditor(String id, PropertyDescriptor descriptor, IModel<T> propertyModel) {
		super(id, propertyModel);
		
		this.descriptor = descriptor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new INullAcceptingValidator<T>() {

			@Override
			public void validate(IValidatable<T> validatable) {
				ComponentContext.push(new ComponentContext(PropertyEditor.this));
				try {
					Validator validator = AppLoader.getInstance(Validator.class);
					Set<?> violations = validator.validateValue(
							descriptor.getBeanClass(), 
							descriptor.getPropertyName(), 
							validatable.getValue());
					
					for (Object each: violations) {
						ConstraintViolation<?> violation = (ConstraintViolation<?>) each;
						error(violation.getMessage());
					}
				} finally {
					ComponentContext.pop();
				}
			}
			
		});
		
		add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String classes = " property-editor editable ";
				if (hasErrorMessage())
					classes += "has-error";
				return classes;
			}
			
		}));
		
		setOutputMarkupId(true);
	}
	
	protected void markFormDirty(AjaxRequestTarget target) {
		String script = String.format(""
				+ "var $form = $('#%s').closest('form');"
				+ "if ($form.closest('.blob-edit').length == 0)"
				+ "  onedev.server.form.markDirty($form);", 
				getMarkupId());
		target.prependJavaScript(script);
	}
	
	protected void onPropertyUpdating(IPartialPageRequestHandler target) {
		validate();
		if (isValid()) 
			send(getParent(), Broadcast.BUBBLE, new PropertyUpdating(target, descriptor.getPropertyName()));								
		else
			clearErrors();
	}
	
	public PropertyDescriptor getDescriptor() {
		return descriptor;
	}

}

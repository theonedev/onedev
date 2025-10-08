package io.onedev.server.web.editable;

import static io.onedev.server.web.translation.Translation._T;

import java.lang.reflect.Method;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.INullAcceptingValidator;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.util.WicketUtils;

public abstract class PropertyEditor<T> extends ValueEditor<T> {

	protected final PropertyDescriptor descriptor;
	
	public PropertyEditor(String id, PropertyDescriptor descriptor, IModel<T> propertyModel) {
		super(id, propertyModel);
		
		this.descriptor = descriptor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add((INullAcceptingValidator<T>) validatable -> {
			ComponentContext.push(new ComponentContext(PropertyEditor.this));
			try {
				Validator validator = AppLoader.getInstance(Validator.class);
				Set<?> violations = validator.validateValue(
						descriptor.getBeanClass(), descriptor.getPropertyName(), validatable.getValue());
				for (Object each: violations) {
					ConstraintViolation<?> violation = (ConstraintViolation<?>) each;
					error(_T(violation.getMessage()));
				}
			} finally {
				ComponentContext.pop();
			}
		});
		
		add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String classes = " property-editor editable ";
				
				// Do not call !isValid() here as otherwise all parent containers with invalid inputs 
				// will also be marked as is-invalid
				if (hasErrorMessage() && getInvalidClass() != null) {
					if (visitChildren(PropertyEditor.class, (IVisitor<PropertyEditor<?>, PropertyEditor<?>>) (object, visit) -> visit.stop(object)) == null) {
						classes += getInvalidClass();
					}
				}
				return classes;
			}
			
		}));
		
		if (descriptor.isSubscriptionRequired() && !WicketUtils.isSubscriptionActive())
			add(AttributeAppender.append("class", "disabled"));
		setOutputMarkupId(true);
	}
	
	@Nullable
	protected String getInvalidClass() {
		return "is-invalid";
	}
	
	protected void markFormDirty(AjaxRequestTarget target) {
		String script = String.format(""
				+ "var $form = $('#%s').closest('form');"
				+ "onedev.server.form.markDirty($form);", 
				getMarkupId());
		target.prependJavaScript(script);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		var script = String.format("" +
				"var $propertyEditor = $('#%s');" +
				"if ($propertyEditor.closest('.disabled').length != 0)" +
				"  $propertyEditor.find('input').addBack('input').attr('disabled', 'disabled')", 
				getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected void onPropertyUpdating(IPartialPageRequestHandler target) {
		convertInput();
		clearErrors();
		
		/**
		 * Bump up event even if some properties are invalid as we may need to do something with 
		 * partial properties of the bean. For instance to update issue description template
		 */
		send(getParent(), Broadcast.BUBBLE, new PropertyUpdating(target, descriptor.getPropertyName()));								
	}
	
	public PropertyDescriptor getDescriptor() {
		return descriptor;
	}

	public abstract boolean needExplicitSubmit();
	
	protected AttributeModifier newPlaceholderModifier() {
		return AttributeModifier.replace("placeholder", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				ComponentContext.push(new ComponentContext(PropertyEditor.this));
				try {
					Method getter = descriptor.getPropertyGetter();
					
					String placeholder = EditableUtils.getPlaceholder(getter);
					if (placeholder != null) 
						return placeholder;
					else if (getter.getAnnotation(OmitName.class) != null)  
						return _T(EditableUtils.getDisplayName(getter));
					else 
						return "";
				} finally {
					ComponentContext.pop();
				}
			}
			
		});		
	}
}

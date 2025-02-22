package io.onedev.server.web.editable.polymorphic;

import io.onedev.server.annotation.ExcludedProperties;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.component.polymorphiceditor.PolymorphicEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PolymorphicPropertyEditor extends PropertyEditor<Serializable> {
	
	private final Set<String> excludedProperties = new HashSet<>();
	
	private PolymorphicEditor editor;
	
	public PolymorphicPropertyEditor(String id, PropertyDescriptor descriptor, IModel<Serializable> propertyModel) {
		super(id, descriptor, propertyModel);
		
		ExcludedProperties excludedPropertiesAnnotation = 
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) 
			Collections.addAll(excludedProperties, excludedPropertiesAnnotation.value());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(editor = new PolymorphicEditor("editor", 
				(Class<? extends Serializable>) descriptor.getPropertyClass(), 
				Model.of(getModelObject())) {
			
			@Override
			protected Set<String> getExcludedProperties() {
				return excludedProperties;
			}

			@Override
			protected boolean isNullValid() {
				return !getDescriptor().isPropertyRequired();				
			}

			@Override
			protected String getNullValidPlaceholder() {
				return EditableUtils.getPlaceholder(getDescriptor().getPropertyGetter());				
			}

			@Override
			protected void onTypeChanging(AjaxRequestTarget target) {
				onPropertyUpdating(target);
				if (editor.isDefined()) {
					target.appendJavaScript(String.format("$('#%s').addClass('property-defined');",
							PolymorphicPropertyEditor.this.getMarkupId()));
				} else {
					target.appendJavaScript(String.format("$('#%s').removeClass('property-defined');",
							PolymorphicPropertyEditor.this.getMarkupId()));
				}
			}
			
		});

		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (editor.isDefined())
					return "property-polymorphic property-defined";
				else
					return "property-polymorphic";
			}

		}));
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof BeanUpdating) {
			event.stop();
			onPropertyUpdating(((BeanUpdating)event.getPayload()).getHandler());
		}		
	}
	
	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		editor.error(propertyNode, pathInProperty, errorMessage);
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		return editor.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}

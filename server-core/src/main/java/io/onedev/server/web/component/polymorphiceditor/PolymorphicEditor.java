package io.onedev.server.web.component.polymorphiceditor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.annotation.ImplementationProvider;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.ValueEditor;

public class PolymorphicEditor extends ValueEditor<Serializable> {

	private final Class<? extends Serializable> baseClass;
	
	private final List<Class<? extends Serializable>> implementations = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	public PolymorphicEditor(String id, Class<? extends Serializable> baseClass, IModel<Serializable> model) {
		super(id, model);

		this.baseClass = baseClass;

		var implementationProvider = baseClass.getAnnotation(ImplementationProvider.class);
		if (implementationProvider != null) {
			implementations.addAll((Collection<? extends Class<? extends Serializable>>) ReflectionUtils.invokeStaticMethod(baseClass, implementationProvider.value()));
		} else {
			ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
			implementations.addAll(registry.getImplementations(baseClass));
		}

		Preconditions.checkArgument(
				!implementations.isEmpty(),
				"Can not find implementations for '" + baseClass + "'.");

		EditableUtils.sortAnnotatedElements(implementations);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: implementations)
			implementationNames.add(EditableUtils.getDisplayName(each));
		
		WebMarkupContainer typeSelectorContainer = new WebMarkupContainer("typeSelectorContainer");
		typeSelectorContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasErrorMessage())
					return " is-invalid";
				else
					return "";
			}

		}));

		add(typeSelectorContainer);

		DropDownChoice<String> typeSelector = new DropDownChoice<>("typeSelector", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				Component beanEditor = PolymorphicEditor.this.get("beanEditor");
				if (beanEditor instanceof BeanEditor)
					return EditableUtils.getDisplayName(((BeanEditor) beanEditor).getDescriptor().getBeanClass());
				else
					return null;
			}

			@Override
			public void setObject(String object) {
				Serializable bean = null;
				for (Class<?> each : implementations) {
					if (EditableUtils.getDisplayName(each).equals(object)) {
						try {
							bean = (Serializable) each.getDeclaredConstructor().newInstance();
							Serializable prevBean = PolymorphicEditor.this.getConvertedInput();
							if (prevBean != null) 
								new BeanDescriptor(baseClass).copyProperties(prevBean, bean);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							throw new RuntimeException(e);
						}
						break;
					}
				}
				PolymorphicEditor.this.replace(newBeanEditor(bean));
			}

		}, implementationNames, new ChoiceRenderer<String>() {

			@Override
			public Object getDisplayValue(String object) {
				return getString("t: " + object);
			}
			
		}) {

			@Override
			protected String getNullValidDisplayValue() {
				ComponentContext.push(new ComponentContext(PolymorphicEditor.this));
				try {
					String placeholder = getNullValidPlaceholder();
					if (placeholder != null)
						return placeholder;
					else
						return super.getNullValidDisplayValue();
				} finally {
					ComponentContext.pop();
				}
			}

		};
		
		typeSelector.setNullValid(isNullValid());
		
		typeSelector.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onTypeChanging(target);
				target.add(typeSelectorContainer.get("typeDescription"));
				Component beanEditor = get("beanEditor");
				target.add(beanEditor);
			}

		});
		typeSelectorContainer.add(typeSelector);
		
		typeSelectorContainer.add(new Label("typeDescription", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Component beanEditor = PolymorphicEditor.this.get("beanEditor");
				if (beanEditor instanceof BeanEditor) {
					Class<?> beanClass = ((BeanEditor) beanEditor).getDescriptor().getBeanClass();
					return EditableUtils.getDescription(beanClass);
				} else {
					return null;
				}
			}

		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				Component beanEditor = PolymorphicEditor.this.get("beanEditor");
				if (beanEditor instanceof BeanEditor) {
					Class<?> beanClass = ((BeanEditor) beanEditor).getDescriptor().getBeanClass();
					setVisible(EditableUtils.getDescription(beanClass) != null);
				} else {
					setVisible(false);
				}
			}

		}.setOutputMarkupPlaceholderTag(true).setEscapeModelStrings(false));

		add(newBeanEditor(getModelObject()));
	}
	
	private Component newBeanEditor(Serializable bean) {
		Component beanEditor;
		if (bean != null) {
			beanEditor = BeanContext.edit("beanEditor", bean, getExcludedProperties(), true);
		} else {
			beanEditor = new WebMarkupContainer("beanEditor");
		}
		beanEditor.setOutputMarkupPlaceholderTag(true);
		return beanEditor;
	}
	
	public boolean isDefined() {
		return get("beanEditor") instanceof BeanEditor;
	}

	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		Component editor = get("beanEditor");
		if (editor instanceof BeanEditor)
			((BeanEditor) editor).error(propertyNode, pathInProperty, errorMessage);
		else
			super.error(propertyNode, pathInProperty, errorMessage);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof BeanUpdating) {
			convertInput();
			clearErrors();
		}
	}
	
	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		Component beanEditor = get("beanEditor");
		if (beanEditor instanceof BeanEditor)
			return ((BeanEditor) beanEditor).getConvertedInput();
		else
			return null;
	}

	protected boolean isNullValid() {
		return true;
	}
	
	@Nullable
	protected String getNullValidPlaceholder() {
		return null;
	}
	
	protected Set<String> getExcludedProperties() {
		return new HashSet<>();
	}
	
	protected void onTypeChanging(AjaxRequestTarget target) {
	}
	
}

package io.onedev.server.web.editable.drawcardbeanlist;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

/**
 * Reusable {@link EditSupport} base for list properties whose elements are edited via a modal
 * form and viewed in a summary table whose rows expand into a {@link io.onedev.server.web.component.draw.DrawCardPanel
 * DrawCardPanel} detail. Subclasses bind a single concrete element type and supply the
 * type-specific list view/edit panels.
 */
public abstract class DrawCardBeanListEditSupport<T extends Serializable> implements EditSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (List.class.isAssignableFrom(descriptor.getPropertyClass())) {
			Class<?> elementClass = ReflectionUtils.getCollectionElementClass(descriptor.getPropertyGetter().getGenericReturnType());
			if (elementClass == getElementClass()) {
				return new PropertyContext<List<Serializable>>(descriptor) {

					private static final long serialVersionUID = 1L;

					@Override
					public PropertyViewer renderForView(String componentId, IModel<List<Serializable>> model) {
						return new PropertyViewer(componentId, descriptor) {

							private static final long serialVersionUID = 1L;

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
									return newListViewPanel(id, model.getObject());
								} else {
									return new EmptyValueLabel(id) {

										private static final long serialVersionUID = 1L;

										@Override
										protected AnnotatedElement getElement() {
											return propertyDescriptor.getPropertyGetter();
										}

									};
								}
							}

						};
					}

					@Override
					public PropertyEditor<List<Serializable>> renderForEdit(String componentId, IModel<List<Serializable>> model) {
						return newListEditPanel(componentId, descriptor, model);
					}

				};
			}
		}
		return null;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	protected abstract Class<T> getElementClass();

	protected abstract DrawCardBeanListViewPanel<T> newListViewPanel(String id, List<Serializable> elements);

	protected abstract DrawCardBeanListEditPanel<T> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model);

}

package com.pmease.commons.wicket.editable.list.polymorphic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.ImplementationRegistry;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class PolymorphicListPropertyEditor extends PropertyEditor<List<Serializable>> {

	private final List<Class<?>> implementations;
	
	public PolymorphicListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		implementations = new ArrayList<>();
		Class<?> baseClass = EditableUtils.getElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());
		Preconditions.checkNotNull(baseClass);
		
		ImplementationRegistry registry = AppLoader.getInstance(ImplementationRegistry.class);
		implementations.addAll(registry.getImplementations(baseClass));

		Preconditions.checkArgument(
				!implementations.isEmpty(), 
				"Can not find implementations for '" + baseClass + "'.");
		
		EditableUtils.sortAnnotatedElements(implementations);
	}

	@SuppressWarnings("unchecked")
	private List<Serializable> newList() {
		if (ClassUtils.isConcrete(getPropertyDescriptor().getPropertyClass())) {
			try {
				return (List<Serializable>) getPropertyDescriptor().getPropertyClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			return new ArrayList<Serializable>();
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getPropertyDescriptor().isPropertyRequired()) {
			add(new WebMarkupContainer("enable").setVisible(false));
		} else {
			add(new CheckBox("enable", new IModel<Boolean>() {
				
				@Override
				public void detach() {
					
				}
	
				@Override
				public Boolean getObject() {
					return PolymorphicListPropertyEditor.this.get("listEditor").isVisible();
				}
	
				@Override
				public void setObject(Boolean object) {
					PolymorphicListPropertyEditor.this.get("listEditor").setVisible(object);
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("change") {
				
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(PolymorphicListPropertyEditor.this.get("listEditor"));
				}
				
			}));
			
		}
		
		List<Serializable> list = getModelObject();
		if (list == null && getPropertyDescriptor().isPropertyRequired())
			list = newList(); 
		add(newListEditor(list));
	}

	private Component newListEditor(List<Serializable> list) {
		final WebMarkupContainer table = new WebMarkupContainer("listEditor");

		table.setOutputMarkupId(true);
		table.setOutputMarkupPlaceholderTag(true);

		final RepeatingView rows = new RepeatingView("elements");
		table.add(rows);
		
		if (list != null) {
			for (Serializable element: list) {
				addRow(element);
			}
		} else {
			table.setVisible(false);
		}
		
		WebMarkupContainer noElements = new WebMarkupContainer("noElements") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!rows.iterator().hasNext());
			}
			
		};
		
		table.add(noElements);
		
		WebMarkupContainer newRow = new WebMarkupContainer("addElement");
		newRow.add(new AjaxButton("addElement") {

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				Serializable newElement;
				try {
					newElement = (Serializable) implementations.get(0).newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				addRow(newElement);
				target.add(PolymorphicListPropertyEditor.this.get("listEditor"));
			}
			
		}.setDefaultFormProcessing(false));
		
		table.add(newRow);
		
		return table;		
	}

	private void addRow(Serializable element) {
		final RepeatingView rows = (RepeatingView) get("listEditor").get("elements");
		
		final WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
		rows.add(row);
		
		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: implementations)
			implementationNames.add(EditableUtils.getName(each));
				
		row.add(new DropDownChoice<String>("elementTypeSelector", new IModel<String>() {
			
			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				Component elementEditor = row.get("elementEditor");
				if (elementEditor.isVisible()) {
					return EditableUtils.getName(((BeanEditor<?>) elementEditor).getBeanDescriptor().getBeanClass());
				} else {
					return null;
				}
			}

			@Override
			public void setObject(String object) {
				for (Class<?> each: implementations) {
					if (EditableUtils.getName(each).equals(object)) {
						Serializable element;
						try {
							element = (Serializable) each.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
						row.replace(newElementEditor(element));
						return;
					}
				}
				throw new IllegalStateException("Unable to find implementation named " + object);
			}
			
			
		}, implementationNames).setNullValid(false).add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(row.get("elementEditor"));
			}
			
		}));

		Component elementEditor = newElementEditor(element);
		row.add(elementEditor);
		
		row.add(new FencedFeedbackPanel("feedback", elementEditor));		

		row.add(new AjaxButton("deleteElement") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				rows.remove(row);
				target.add(PolymorphicListPropertyEditor.this.get("listEditor"));
			}

		}.setDefaultFormProcessing(false));
	}

	private Component newElementEditor(Serializable element) {
		Component elementEditor;
		if (element != null) {
			elementEditor = BeanContext.edit("elementEditor", element);
		} else {
			elementEditor = new WebMarkupContainer("elementEditor").setVisible(false);
		}
		elementEditor.setOutputMarkupId(true);
		elementEditor.setOutputMarkupPlaceholderTag(true);
		return elementEditor;
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		int index = ((PathSegment.Element) pathSegment).getIndex();

		RepeatingView rows = (RepeatingView) get("listEditor").get("elements");
		
		int currentIndex = 0;
		Iterator<Component> it = rows.iterator();
		Component row = it.next();
		while (currentIndex++ < index) {
			row = it.next();
		}
		
		return (ErrorContext) row.get("elementEditor");
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		if (get("listEditor").isVisible()) {
			List<Serializable> newList = newList();
			RepeatingView rows = (RepeatingView) get("listEditor").get("elements");
			for (Component row: rows) {
				@SuppressWarnings("unchecked")
				BeanEditor<Serializable> elementEditor = (BeanEditor<Serializable>) row.get("elementEditor");
				newList.add(elementEditor.getConvertedInput());
			}
			return newList;
		} else {
			return null;
		}
	}
}

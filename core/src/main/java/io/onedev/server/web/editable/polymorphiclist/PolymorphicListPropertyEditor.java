package io.onedev.server.web.editable.polymorphiclist;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.launcher.loader.ImplementationRegistry;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class PolymorphicListPropertyEditor extends PropertyEditor<List<Serializable>> {

	private final Class<?> baseClass;
	
	private final List<Class<?>> implementations;
	
	private RepeatingView rows;
	
	private WebMarkupContainer noElements;
	
	public PolymorphicListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		implementations = new ArrayList<>();
		baseClass = ReflectionUtils.getCollectionElementType(propertyDescriptor.getPropertyGetter().getGenericReturnType());
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
		if (ClassUtils.isConcrete(getDescriptor().getPropertyClass())) {
			try {
				return (List<Serializable>) getDescriptor().getPropertyClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			return new ArrayList<Serializable>();
		}
	}
	
	private String getDisplayName(Class<?> clazz) {
		String displayName = EditableUtils.getDisplayName(clazz);
		displayName = Application.get().getResourceSettings().getLocalizer().getString(displayName, this, displayName);
		return displayName;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Serializable> list = getModelObject();
		if (list == null)
			list = newList(); 
		
		rows = new RepeatingView("elements");
		add(rows);
		
		for (Serializable element: list) {
			addRow(element);
		}
		
		add(new AjaxButton("addElement") {

			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				Component lastRow;
				if (rows.size() != 0)
					lastRow = rows.get(rows.size() - 1);
				else 
					lastRow = null;
				
				Serializable newElement;
				try {
					newElement = (Serializable) implementations.get(0).newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				Component newRow = addRow(newElement);
				
				String script = String.format("$('<tr id=\"%s\"></tr>')", newRow.getMarkupId());
				if (lastRow != null)
					script += ".insertAfter('#" + lastRow.getMarkupId() + "');";
				else
					script += ".appendTo('#" + PolymorphicListPropertyEditor.this.getMarkupId() + ">div>table>tbody');";

				target.prependJavaScript(script);
				target.add(newRow);
				target.add(noElements);
				
				onPropertyUpdating(target);
			}
			
		}.setDefaultFormProcessing(false));
		
		add(noElements = new WebMarkupContainer("noElements") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(rows.size() == 0);
			}
			
		});
		noElements.setOutputMarkupPlaceholderTag(true);
		
		add(new SortBehavior() {

			@SuppressWarnings("deprecation")
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				/*
				List<Component> children = new ArrayList<>();
				for (Component child: rows)
					children.add(child);

				Component fromChild = children.remove(from.getItemIndex());
				children.add(to.getItemIndex(), fromChild);
				
				rows.removeAll();
				for (Component child: children)
					rows.add(child);
				*/

				// Do not use code above as removing components outside of a container and add again 
				// can cause the fenced feedback panel not functioning properly
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						rows.swap(fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						rows.swap(fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
			}
			
		}.sortable("tbody"));		
		
		setOutputMarkupId(true);
	}

	@Override
	protected String getErrorClass() {
		return null;
	}

	private WebMarkupContainer addRow(Serializable element) {
		WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
		row.setOutputMarkupId(true);
		rows.add(row);
		
		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: implementations) 
			implementationNames.add(getDisplayName(each));
				
		Component elementEditor = newElementEditor(element);
		row.add(elementEditor);
		
		row.add(new DropDownChoice<String>("elementTypeSelector", new IModel<String>() {
			
			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				Component elementEditor = row.get("elementEditor");
				if (elementEditor.isVisible()) {
					return EditableUtils.getDisplayName(((BeanEditor) elementEditor).getBeanDescriptor().getBeanClass());
				} else {
					return null;
				}
			}

			@Override
			public void setObject(String object) {
				for (Class<?> each: implementations) {
					if (getDisplayName(each).equals(object)) {
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
				onPropertyUpdating(target);
			}
			
		}));

		row.add(new FencedFeedbackPanel("feedback", elementEditor));		

		row.add(new AjaxButton("deleteElement") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				target.appendJavaScript(String.format("onedev.server.form.markDirty($('#%s'));", form.getMarkupId(true)));
				target.appendJavaScript($(row).chain("remove").get());
				rows.remove(row);
				target.add(noElements);

				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));
		
		return row;
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof BeanUpdating) {
			event.stop();
			onPropertyUpdating(((BeanUpdating)event.getPayload()).getHandler());
		}		
	}
	
	private Component newElementEditor(Serializable element) {
		Component elementEditor;
		if (element != null) {
			elementEditor = BeanContext.editBean("elementEditor", element);
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
		List<Serializable> newList = newList();
		for (Component row: rows) {
			BeanEditor elementEditor = (BeanEditor) row.get("elementEditor");
			newList.add(elementEditor.getConvertedInput());
		}
		return newList;
	}
	
}

package io.onedev.server.web.editable.polymorphiclist;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.launcher.loader.ImplementationRegistry;
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
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.Vertical;
import io.onedev.utils.ClassUtils;

@SuppressWarnings("serial")
public class PolymorphicListPropertyEditor extends PropertyEditor<List<Serializable>> {

	private final List<Class<?>> implementations;
	
	private final boolean horizontal;
	
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

		Method propertyGetter = propertyDescriptor.getPropertyGetter();
		if (propertyGetter.getAnnotation(Horizontal.class) != null)
			horizontal = true;
		else if (propertyGetter.getAnnotation(Vertical.class) != null)
			horizontal = false;
		else 
			horizontal = true;
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

		if (getDescriptor().isPropertyRequired()) {
			add(new WebMarkupContainer("enable").setVisible(false));
		} else {
			CheckBox checkBox = new CheckBox("enable", new IModel<Boolean>() {
				
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
				
			});
			checkBox.add(new AjaxFormComponentUpdatingBehavior("change") {
				
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(PolymorphicListPropertyEditor.this.get("listEditor"));
				}
				
			});
			checkBox.setLabel(Model.of(getDescriptor().getDisplayName(this)));
			add(checkBox);
		}
		
		List<Serializable> list = getModelObject();
		if (list == null && getDescriptor().isPropertyRequired())
			list = newList(); 
		add(newListEditor(list));
	}

	@Override
	protected String getErrorClass() {
		return null;
	}

	private Component newListEditor(List<Serializable> list) {
		final WebMarkupContainer table = new WebMarkupContainer("listEditor");
		if (horizontal)
			table.add(AttributeAppender.append("class", " horizontal"));
		else
			table.add(AttributeAppender.append("class", " vertical"));

		table.setOutputMarkupId(true);
		table.setOutputMarkupPlaceholderTag(true);

		final RepeatingView rows = new RepeatingView("elements");
		table.add(rows);
		
		if (list != null) {
			for (Serializable element: list) {
				addRow(rows, element);
			}
		} else {
			table.setVisible(false);
		}
		
		final WebMarkupContainer noElements = new WebMarkupContainer("noElements") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(rows.size() == 0);
			}
			
		};
		noElements.setOutputMarkupPlaceholderTag(true);
		table.add(noElements);
		
		table.add(new AjaxButton("addElement") {

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
				Component newRow = addRow(rows, newElement);
				
				String script = String.format("$('<tr id=\"%s\"></tr>')", newRow.getMarkupId());
				if (lastRow != null)
					script += ".insertAfter('#" + lastRow.getMarkupId() + "');";
				else
					script += ".appendTo('#" + table.getMarkupId() + ">tbody');";

				target.prependJavaScript(script);
				target.add(newRow);
				
				if (rows.size() == 1)
					target.add(noElements);
				
				onPropertyUpdating(target);
			}
			
		}.setDefaultFormProcessing(false));
		
		table.add(new SortBehavior() {

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
				int fromIndex = from.getItemIndex() - 1;
				int toIndex = to.getItemIndex() - 1;
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						rows.swap(fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						rows.swap(fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
			}
			
		}.sortable("tbody").handle(".handle").helperClass("sort-helper"));

		return table;		
	}

	private WebMarkupContainer addRow(final RepeatingView rows, Serializable element) {
		final Fragment row;
		if (horizontal)
			row = new Fragment(rows.newChildId(), "horizontalFrag", this);
		else 
			row = new Fragment(rows.newChildId(), "verticalFrag", this);
		
		rows.add(row);
		
		List<String> implementationNames = new ArrayList<String>();
		for (Class<?> each: implementations) 
			implementationNames.add(getDisplayName(each));
				
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

		Component elementEditor = newElementEditor(element);
		row.add(elementEditor);
		
		row.add(new FencedFeedbackPanel("feedback", elementEditor));		

		row.add(new AjaxButton("deleteElement") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				target.appendJavaScript(String.format("onedev.server.form.markDirty($('#%s'));", form.getMarkupId(true)));
				target.appendJavaScript($(row).chain("remove").get());
				rows.remove(row);

				if (rows.size() == 0) {
					WebMarkupContainer table = (WebMarkupContainer) PolymorphicListPropertyEditor.this.get("listEditor");
					target.add(table.get("noElements"));
				}
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
				BeanEditor elementEditor = (BeanEditor) row.get("elementEditor");
				newList.add(elementEditor.getConvertedInput());
			}
			return newList;
		} else {
			return null;
		}
	}
	
}

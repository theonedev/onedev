package io.onedev.server.web.editable.revisionchoice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.model.Project;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.revision.RevisionPicker;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.RevisionChoice;

@SuppressWarnings("serial")
public class RevisionChoiceEditor extends PropertyEditor<String> {

	private String revision;
	
	public RevisionChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		revision = propertyModel.getObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newRevisionPicker());
	}
	
	private RevisionPicker newRevisionPicker() {
		return new RevisionPicker("picker", new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				ComponentContext.push(new ComponentContext(RevisionChoiceEditor.this));
				try {
					RevisionChoice revisionChoice = descriptor.getPropertyGetter().getAnnotation(RevisionChoice.class);
					if (revisionChoice.value().length() != 0) 
						return (Project) ReflectionUtils.invokeStaticMethod(descriptor.getBeanClass(), revisionChoice.value());
					else 
						return Project.get();
				} finally {
					ComponentContext.pop();
				}
			}
			
		}, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				RevisionChoiceEditor.this.revision = revision; 
				RevisionPicker revisionPicker = newRevisionPicker();
				getParent().replace(revisionPicker);
				target.add(revisionPicker);
				onPropertyUpdating(target);				
			}
			
		};		
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return revision;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}

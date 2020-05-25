package io.onedev.server.web.editable.revisionpick;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.revisionpicker.RevisionPicker;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class RevisionPickEditor extends PropertyEditor<String> {

	private String revision;
	
	public RevisionPickEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newRevisionPicker());
	}
	
	private RevisionPicker newRevisionPicker() {
		return new RevisionPicker("picker", new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return Project.get();
			}
			
		}, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				RevisionPickEditor.this.revision = revision; 
				RevisionPicker revisionPicker = newRevisionPicker();
				getParent().replace(revisionPicker);
				target.add(revisionPicker);
			}
			
		};		
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return revision;
	}

}

package io.onedev.server.web.editable.issue.choice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.issue.choice.IssueChoiceProvider;
import io.onedev.server.web.component.issue.choice.IssueSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class IssueSingleChoiceEditor extends PropertyEditor<Long> {

	private IssueSingleChoice input;
	
	public IssueSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private Project getProject() {
		return ((ProjectPage)getPage()).getProject();		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Issue issue;
		if (getModelObject() != null)
			issue = OneDev.getInstance(IssueManager.class).find(getProject(), getModelObject());
		else
			issue = null;
		
		IssueChoiceProvider choiceProvider = new IssueChoiceProvider(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
    		
    	});
    	input = new IssueSingleChoice("input", Model.of(issue), choiceProvider) {

    		@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
    		
    	};
        
        // add this to control allowClear flag of select2
    	input.setRequired(descriptor.isPropertyRequired());
        input.setLabel(Model.of(getDescriptor().getDisplayName()));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		add(input);
	}

	@Override
	protected Long convertInputToValue() throws ConversionException {
		Issue issue = input.getConvertedInput();
		if (issue != null)
			return issue.getNumber();
		else
			return null;
	}

}

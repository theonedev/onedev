package io.onedev.server.web.editable.pullrequest.choice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.pullrequest.choice.PullRequestChoiceProvider;
import io.onedev.server.web.component.pullrequest.choice.PullRequestSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class PullRequestSingleChoiceEditor extends PropertyEditor<Long> {

	private PullRequestSingleChoice input;
	
	public PullRequestSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private Project getProject() {
		return ((ProjectPage)getPage()).getProject();		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequest request;
		if (getModelObject() != null)
			request = OneDev.getInstance(PullRequestManager.class).find(getProject(), getModelObject());
		else
			request = null;
		
		PullRequestChoiceProvider choiceProvider = new PullRequestChoiceProvider(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
    		
    	});
    	input = new PullRequestSingleChoice("input", Model.of(request), choiceProvider) {

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
		PullRequest request = input.getConvertedInput();
		if (request != null)
			return request.getNumber();
		else
			return null;
	}

}

package io.onedev.server.web.editable.pullrequest.choice;

import static io.onedev.server.web.translation.Translation._T;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.web.component.pullrequest.choice.PullRequestChoiceProvider;
import io.onedev.server.web.component.pullrequest.choice.PullRequestSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.ProjectAware;

public class PullRequestChoiceEditor extends PropertyEditor<Long> {

	@Inject
	private PullRequestService pullRequestService;

	private PullRequestSingleChoice input;

	private final boolean useNumber;
	
	public PullRequestChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<Long> propertyModel, boolean useNumber) {
		super(id, propertyDescriptor, propertyModel);
		this.useNumber = useNumber;
	}

	private Project getProject() {
		ProjectAware projectAware = findParent(ProjectAware.class);
		if (projectAware != null)
			return projectAware.getProject();		
		else
			return null;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequest request = null;
		var pullRequestIdOrNumber = getModelObject();
		if (pullRequestIdOrNumber != null) {
			if (useNumber) {
				Preconditions.checkState(getProject() != null);
				request = pullRequestService.find(getProject(), pullRequestIdOrNumber);
			} else {
				request = pullRequestService.get(pullRequestIdOrNumber);
			}
		}
		
		PullRequestChoiceProvider choiceProvider = new PullRequestChoiceProvider(useNumber) {


			@Override
			protected Project getProject() {
				return PullRequestChoiceEditor.this.getProject();
			}
			
		};
    	input = new PullRequestSingleChoice("input", Model.of(request), choiceProvider) {

    		@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
			}
    		
    	};
        
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
        
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
			return useNumber? request.getNumber() : request.getId();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}

package io.onedev.server.web.editable.issue.choice;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.web.component.issue.choice.IssueSingleChoice;
import io.onedev.server.web.component.issue.choice.IssueChoiceProvider;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.IssueQueryAware;
import io.onedev.server.web.util.ProjectAware;

public class IssueChoiceEditor extends PropertyEditor<Long> {

	private IssueSingleChoice input;
	
	public IssueChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
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

		Issue issue;
		if (getModelObject() != null)
			issue = OneDev.getInstance(IssueService.class).get(getModelObject());
		else
			issue = null;
		
		IssueChoiceProvider choiceProvider = new IssueChoiceProvider() {

			@Override
			protected Project getProject() {
				return IssueChoiceEditor.this.getProject();
			}

			@Override
			protected IssueQuery getBaseQuery() {
				IssueQueryAware issueScopeAware = findParent(IssueQueryAware.class);
				if (issueScopeAware != null) 
					return issueScopeAware.getIssueQuery();
				else
					return null;
			}
			
		};
    	input = new IssueSingleChoice("input", Model.of(issue), choiceProvider) {

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
		Issue issue = input.getConvertedInput();
		if (issue != null)
			return issue.getId();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}

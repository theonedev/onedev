package io.onedev.server.web.editable.branchchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.branch.choice.BranchChoiceProvider;
import io.onedev.server.web.component.branch.choice.BranchMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class BranchMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private BranchMultiChoice input;
	
	public BranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	BranchChoiceProvider branchProvider = new BranchChoiceProvider(new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				if (getPage() instanceof ProjectPage)
					return ((ProjectPage) getPage()).getProject();
				else
					return null;
			}
    		
    	});

    	ArrayList<String> projectAndBranches = new ArrayList<>();
		if (getModelObject() != null) 
			projectAndBranches.addAll(getModelObject());
		
		input = new BranchMultiChoice("input", new Model(projectAndBranches), branchProvider) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor, this);
			}
			
		};
        input.setLabel(Model.of(getDescriptor().getDisplayName()));
        
        input.setRequired(descriptor.isPropertyRequired());
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
        add(input);
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> projectAndBranches = new ArrayList<>();
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null) 
			projectAndBranches.addAll(convertedInput);
		return projectAndBranches;
	}

}

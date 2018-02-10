package com.turbodev.server.web.editable.branchchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.turbodev.server.model.Project;
import com.turbodev.server.web.component.branchchoice.BranchChoiceProvider;
import com.turbodev.server.web.component.branchchoice.BranchMultiChoice;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;
import com.turbodev.server.web.page.project.ProjectPage;

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
				ProjectPage page = (ProjectPage) getPage();
				return page.getProject();
			}
    		
    	});

    	ArrayList<String> projectAndBranches = new ArrayList<>();
		if (getModelObject() != null) 
			projectAndBranches.addAll(getModelObject());
		
		input = new BranchMultiChoice("input", new Model(projectAndBranches), branchProvider);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
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

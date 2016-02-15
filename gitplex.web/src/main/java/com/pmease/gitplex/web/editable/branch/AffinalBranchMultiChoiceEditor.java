package com.pmease.gitplex.web.editable.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.web.component.branchchoice.AffinalBranchMultiChoice;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class AffinalBranchMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private AffinalBranchMultiChoice input;
	
	public AffinalBranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new AffinalBranchMultiChoice("input", ((RepositoryPage)getPage()).getDepot().getId(), new LoadableDetachableModel() {

			@Override
			protected Object load() {
				List<String> repoAndBranches = getModelObject();
				if (repoAndBranches != null)
					return repoAndBranches;
				else
					return new ArrayList<>();
			}
    		
    	});
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> repoAndBranches = new ArrayList<>();
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null)
			repoAndBranches.addAll(convertedInput);
		return repoAndBranches;
	}

}

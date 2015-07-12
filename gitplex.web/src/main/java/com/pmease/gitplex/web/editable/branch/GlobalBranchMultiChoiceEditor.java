package com.pmease.gitplex.web.editable.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.web.component.branch.GlobalBranchMultiChoice;

@SuppressWarnings("serial")
public class GlobalBranchMultiChoiceEditor extends PropertyEditor<List<RepoAndBranch>> {
	
	private GlobalBranchMultiChoice input;
	
	public GlobalBranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<RepoAndBranch>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new GlobalBranchMultiChoice("input", new LoadableDetachableModel<Collection<RepoAndBranch>>() {

			@Override
			protected Collection<RepoAndBranch> load() {
		    	List<RepoAndBranch> branches = new ArrayList<>();
				if (getModelObject() != null)
					branches.addAll(getModelObject());
				
				return branches;
			}
    		
    	});
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<RepoAndBranch> convertInputToValue() throws ConversionException {
		List<RepoAndBranch> repoAndBranches = new ArrayList<>();
		
		Collection<RepoAndBranch> convertedInput = input.getConvertedInput();
		if (convertedInput != null)
			repoAndBranches.addAll(convertedInput);
		
		return repoAndBranches;
	}

}

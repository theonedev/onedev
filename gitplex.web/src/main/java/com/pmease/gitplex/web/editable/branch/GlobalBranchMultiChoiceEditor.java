package com.pmease.gitplex.web.editable.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.web.component.branch.GlobalBranchMultiChoice;

@SuppressWarnings("serial")
public class GlobalBranchMultiChoiceEditor extends PropertyEditor<List<Long>> {
	
	private GlobalBranchMultiChoice input;
	
	public GlobalBranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Long>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new GlobalBranchMultiChoice("input", new LoadableDetachableModel<Collection<Branch>>() {

			@Override
			protected Collection<Branch> load() {
		    	List<Branch> branches = new ArrayList<>();
				if (getModelObject() != null) {
					Dao dao = GitPlex.getInstance(Dao.class);
					for (Long branchId: getModelObject()) 
						branches.add(dao.load(Branch.class, branchId));
				}
				
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
	protected List<Long> convertInputToValue() throws ConversionException {
		List<Long> branchIds = new ArrayList<>();
		Collection<Branch> branches = input.getConvertedInput();
		if (branches != null) {
			for (Branch branch: branches)
				branchIds.add(branch.getId());
		} 
		return branchIds;
	}

}

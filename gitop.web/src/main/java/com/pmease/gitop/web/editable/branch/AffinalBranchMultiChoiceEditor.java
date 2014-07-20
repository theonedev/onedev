package com.pmease.gitop.web.editable.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.component.branch.AffinalBranchMultiChoice;
import com.pmease.gitop.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class AffinalBranchMultiChoiceEditor extends PropertyEditor<List<Long>> {
	
	private AffinalBranchMultiChoice input;
	
	public AffinalBranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Long>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new AffinalBranchMultiChoice("input", new AbstractReadOnlyModel<Repository>() {

			@Override
			public Repository getObject() {
				RepositoryPage page = (RepositoryPage) getPage();
				return page.getRepository();
			}
    		
    	}, new LoadableDetachableModel() {

			@Override
			protected Object load() {
		    	List<Branch> branches = new ArrayList<>();
				if (getModelObject() != null) {
					Dao dao = Gitop.getInstance(Dao.class);
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

package com.pmease.gitop.web.editable.branch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.web.component.branch.BranchChoiceProvider;
import com.pmease.gitop.web.component.branch.BranchMultiChoice;
import com.pmease.gitop.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class BranchMultiChoiceEditor extends PropertyEditor<List<Long>> {
	
	private BranchMultiChoice input;
	
	public BranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Long>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	BranchChoiceProvider branchProvider = new BranchChoiceProvider(new LoadableDetachableModel<EntityCriteria<Branch>>() {

			@Override
			protected EntityCriteria<Branch> load() {
				EntityCriteria<Branch> criteria = EntityCriteria.of(Branch.class);
				RepositoryPage page = (RepositoryPage) getPage();
				criteria.add(Restrictions.eq("repository", page.getRepository()));
				return criteria;
			}
    		
    	});

    	List<Branch> branches = new ArrayList<>();
		if (getModelObject() != null) {
			Dao dao = Gitop.getInstance(Dao.class);
			for (Long branchId: getModelObject()) 
				branches.add(dao.load(Branch.class, branchId));
		}
		
		input = new BranchMultiChoice("input", new Model((Serializable)branches), branchProvider);
        
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

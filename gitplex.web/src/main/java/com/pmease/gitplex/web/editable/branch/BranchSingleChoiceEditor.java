package com.pmease.gitplex.web.editable.branch;

import com.pmease.gitplex.core.GitPlex;
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
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.web.component.branch.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branch.BranchSingleChoice;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class BranchSingleChoiceEditor extends PropertyEditor<Long> {
	
	private BranchSingleChoice input;
	
	public BranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

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

    	Branch branch;
		if (getModelObject() != null)
			branch =  GitPlex.getInstance(Dao.class).load(Branch.class, getModelObject());
		else
			branch = null;
    	input = new BranchSingleChoice("input", Model.of(branch), branchProvider);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected Long convertInputToValue() throws ConversionException {
		Branch branch = input.getConvertedInput();
		if (branch != null)
			return branch.getId();
		else
			return null;
	}

}

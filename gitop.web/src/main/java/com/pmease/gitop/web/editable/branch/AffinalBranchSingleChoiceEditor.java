package com.pmease.gitop.web.editable.branch;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.component.branch.AffinalBranchSingleChoice;
import com.pmease.gitop.web.page.repository.RepositoryBasePage;

@SuppressWarnings("serial")
public class AffinalBranchSingleChoiceEditor extends PropertyEditor<Long> {
	
	private AffinalBranchSingleChoice input;
	
	public AffinalBranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	Branch branch;
		if (getModelObject() != null)
			branch =  Gitop.getInstance(Dao.class).load(Branch.class, getModelObject()); 
		else
			branch = null;

		input = new AffinalBranchSingleChoice("input", new AbstractReadOnlyModel<Repository>() {

			@Override
			public Repository getObject() {
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				return page.getRepository();
			}
    		
    	}, Model.of(branch));
        
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

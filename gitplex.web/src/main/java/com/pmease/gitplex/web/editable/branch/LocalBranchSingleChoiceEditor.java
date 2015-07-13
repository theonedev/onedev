package com.pmease.gitplex.web.editable.branch;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branchchoice.BranchSingleChoice;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class LocalBranchSingleChoiceEditor extends PropertyEditor<RepoAndBranch> {
	
	private BranchSingleChoice input;
	
	public LocalBranchSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<RepoAndBranch> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
    	BranchChoiceProvider branchProvider = new BranchChoiceProvider(new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				RepositoryPage page = (RepositoryPage) getPage();
				return page.getRepository();
			}
    		
    	});

    	input = new BranchSingleChoice("input", getModel(), branchProvider);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected RepoAndBranch convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}

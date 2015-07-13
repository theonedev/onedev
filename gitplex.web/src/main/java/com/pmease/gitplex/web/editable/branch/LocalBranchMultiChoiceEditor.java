package com.pmease.gitplex.web.editable.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branchchoice.BranchMultiChoice;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class LocalBranchMultiChoiceEditor extends PropertyEditor<List<RepoAndBranch>> {
	
	private BranchMultiChoice input;
	
	public LocalBranchMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<RepoAndBranch>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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

    	ArrayList<RepoAndBranch> repoAndBranches = new ArrayList<>();
		if (getModelObject() != null) 
			repoAndBranches.addAll(getModelObject());
		
		input = new BranchMultiChoice("input", new Model(repoAndBranches), branchProvider);
        
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

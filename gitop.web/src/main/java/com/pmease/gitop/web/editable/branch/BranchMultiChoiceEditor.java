package com.pmease.gitop.web.editable.branch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.web.component.choice.BranchChoiceProvider;
import com.pmease.gitop.web.component.choice.BranchMultiChoice;
import com.pmease.gitop.web.page.project.RepositoryBasePage;

@SuppressWarnings("serial")
public class BranchMultiChoiceEditor extends Panel {
	
	private final BranchMultiChoiceEditContext editContext;

	public BranchMultiChoiceEditor(String id, BranchMultiChoiceEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	IModel<Collection<Branch>> model = new IModel<Collection<Branch>>() {

			@Override
			public void detach() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public Collection<Branch> getObject() {
				List<Long> branchIds = (List<Long>) editContext.getPropertyValue();
				if (branchIds != null) {
					BranchManager branchManager = Gitop.getInstance(BranchManager.class);
					Collection<Branch> branches = new ArrayList<>();
					for (Long branchId: branchIds) 
						branches.add(branchManager.load(branchId));
					return branches;
				} else {
					return null;
				}
			}

			@Override
			public void setObject(Collection<Branch> branches) {
				if (branches != null) {
					List<Long> branchIds = new ArrayList<>();
					for (Branch branch: branches)
						branchIds.add(branch.getId());
					editContext.setPropertyValue((Serializable) branchIds);
				} else {
					editContext.setPropertyValue(null);
				}
			}
    		
    	};
    	
    	BranchChoiceProvider branchProvider = new BranchChoiceProvider(new LoadableDetachableModel<DetachedCriteria>() {

			@Override
			protected DetachedCriteria load() {
				DetachedCriteria criteria = DetachedCriteria.forClass(Branch.class);
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				criteria.add(Restrictions.eq("project", page.getProject()));
				return criteria;
			}
    		
    	});

    	BranchMultiChoice chooser = new BranchMultiChoice("chooser", model, branchProvider);
        chooser.setConvertEmptyInputStringToNull(true);
        
        add(chooser);
	}

}

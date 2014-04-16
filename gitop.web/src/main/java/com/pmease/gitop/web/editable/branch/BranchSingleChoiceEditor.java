package com.pmease.gitop.web.editable.branch;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.web.component.choice.BranchChoiceProvider;
import com.pmease.gitop.web.component.choice.BranchSingleChoice;
import com.pmease.gitop.web.page.repository.RepositoryBasePage;

@SuppressWarnings("serial")
public class BranchSingleChoiceEditor extends Panel {
	
	private final BranchSingleChoiceEditContext editContext;

	public BranchSingleChoiceEditor(String id, BranchSingleChoiceEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	IModel<Branch> model = new IModel<Branch>() {

			@Override
			public void detach() {
			}

			@Override
			public Branch getObject() {
				Long branchId = (Long) editContext.getPropertyValue();
				if (branchId != null)
					return Gitop.getInstance(BranchManager.class).load(branchId); 
				else
					return null;
			}

			@Override
			public void setObject(Branch object) {
				if (object != null)
					editContext.setPropertyValue(object.getId());
				else
					editContext.setPropertyValue(null);
			}
    		
    	};
    	
    	BranchChoiceProvider branchProvider = new BranchChoiceProvider(new LoadableDetachableModel<DetachedCriteria>() {

			@Override
			protected DetachedCriteria load() {
				DetachedCriteria criteria = DetachedCriteria.forClass(Branch.class);
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				criteria.add(Restrictions.eq("repository", page.getRepository()));
				return criteria;
			}
    		
    	});

    	BranchSingleChoice chooser = new BranchSingleChoice("chooser", model, branchProvider);
        chooser.setConvertEmptyInputStringToNull(true);
        
        add(chooser);
	}

}

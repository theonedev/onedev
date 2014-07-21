package com.pmease.gitplex.web.component.branch;

import java.util.Collection;
import java.util.HashSet;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repository.ComparableRepositoryChoice;

@SuppressWarnings("serial")
public class AffinalBranchMultiChoice extends FormComponentPanel<Collection<Branch>> {

	private IModel<Repository> currentRepositoryModel;
	
	private IModel<Repository> selectedRepositoryModel;
	
	private BranchMultiChoice branchChoicer;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param currentRepositoryModel
	 * 			model of current repository. Note that the model object should never be null
	 * @param selectedBranchesModel
	 * 			model of selected branch
	 */
	public AffinalBranchMultiChoice(String id, IModel<Repository> currentRepositoryModel, 
			IModel<Collection<Branch>> selectedBranchesModel) {
		super(id, selectedBranchesModel);
		
		this.currentRepositoryModel = currentRepositoryModel;
		
		selectedRepositoryModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				Collection<Branch> branches = getBranches();
				if (branches == null || branches.isEmpty()) {
					return AffinalBranchMultiChoice.this.currentRepositoryModel.getObject();
				} else {
					return branches.iterator().next().getRepository();
				}
			}

			@Override
			public void setObject(Repository object) {
				setBranches(new HashSet<Branch>());
			}
			
		};
		
	}
	
	protected void onChange(AjaxRequestTarget target) {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new ComparableRepositoryChoice("repositoryChoice", currentRepositoryModel, selectedRepositoryModel).add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(AffinalBranchMultiChoice.this);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(new LoadableDetachableModel<EntityCriteria<Branch>>() {

			@Override
			protected EntityCriteria<Branch> load() {
				EntityCriteria<Branch> criteria = EntityCriteria.of(Branch.class);
				criteria.add(Restrictions.eq("repository", selectedRepositoryModel.getObject()));
				return criteria;
			}
			
		});
		
		add(branchChoicer = new BranchMultiChoice("branchChoice", getModel(), choiceProvider));
		branchChoicer.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target);
			}
			
		});
	}
	
	private Collection<Branch> getBranches() {
		return getModelObject();
	}

	private void setBranches(Collection<Branch> branches) {
		getModel().setObject(branches);
	}

	@Override
	protected void convertInput() {
		setConvertedInput(branchChoicer.getConvertedInput());
	}

	@Override
	protected void onDetach() {
		currentRepositoryModel.detach();
		selectedRepositoryModel.detach();
		
		super.onDetach();
	}
	
}

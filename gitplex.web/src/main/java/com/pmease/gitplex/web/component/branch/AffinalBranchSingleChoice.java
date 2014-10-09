package com.pmease.gitplex.web.component.branch;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repository.ComparableRepositoryChoice;

@SuppressWarnings("serial")
public class AffinalBranchSingleChoice extends FormComponentPanel<Branch> {

	private IModel<Repository> currentRepositoryModel;
	
	private IModel<Repository> selectedRepositoryModel;
	
	private BranchSingleChoice branchChoicer;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param currentRepositoryModel
	 * 			model of current repository. Note that the model object should never be null
	 * @param selectedBranchModel
	 * 			model of selected branch
	 */
	public AffinalBranchSingleChoice(String id, IModel<Repository> currentRepositoryModel, IModel<Branch> selectedBranchModel) {
		super(id, selectedBranchModel);
		
		this.currentRepositoryModel = currentRepositoryModel;
		
		selectedRepositoryModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				Branch branch = getBranch();
				if (branch == null) {
					return AffinalBranchSingleChoice.this.currentRepositoryModel.getObject();
				} else {
					return branch.getRepository();
				}
			}

			@Override
			public void setObject(Repository object) {
				setBranch(GitPlex.getInstance(BranchManager.class).findDefault(object));
			}
			
		};
		
	}
	
	protected void onChange(AjaxRequestTarget target) {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new ComparableRepositoryChoice("repositoryChoice", currentRepositoryModel, selectedRepositoryModel).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(AffinalBranchSingleChoice.this);
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
		add(branchChoicer = new BranchSingleChoice("branchChoice", getModel(), choiceProvider));
		branchChoicer.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target);
			}
			
		});
	}
	
	private Branch getBranch() {
		return getModelObject();
	}

	private void setBranch(Branch branch) {
		getModel().setObject(branch);
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

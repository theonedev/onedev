package com.pmease.gitplex.web.component.branchchoice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.DepotAndBranch;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.repochoice.AffinalDepotChoice;

@SuppressWarnings("serial")
public class AffinalBranchSingleChoice extends FormComponentPanel<String> {

	private final Long currentRepoId;
	
	private final IModel<Depot> selectedRepoModel;
	
	private final boolean allowEmpty;
	
	private BranchSingleChoice branchChoice;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param currentRepoId
	 * 			id of current repository
	 * @param selectedBranchModel
	 * 			model of selected branch
	 */
	public AffinalBranchSingleChoice(String id, final Long currentRepoId, 
			IModel<String> selectedBranchModel, boolean allowEmpty) {
		super(id, selectedBranchModel);
		
		this.currentRepoId = currentRepoId;
		
		selectedRepoModel = new IModel<Depot>() {

			@Override
			public void detach() {
			}

			@Override
			public Depot getObject() {
				String branchId = getBranchId();
				if (branchId == null)
					return GitPlex.getInstance(Dao.class).load(Depot.class, currentRepoId);
				else 
					return new DepotAndBranch(branchId).getDepot();
			}

			@Override
			public void setObject(Depot object) {
				setBranchId(new DepotAndBranch(object, object.getDefaultBranch()).toString());
			}
			
		};
		
		this.allowEmpty = allowEmpty;
	}
	
	protected void onChange(AjaxRequestTarget target) {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new AffinalDepotChoice("repositoryChoice", currentRepoId, selectedRepoModel).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchChoice);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(selectedRepoModel);
		add(branchChoice = new BranchSingleChoice("branchChoice", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return new DepotAndBranch(AffinalBranchSingleChoice.this.getModelObject()).getBranch();
			}

			@Override
			public void setObject(String object) {
				AffinalBranchSingleChoice.this.setModelObject(new DepotAndBranch(selectedRepoModel.getObject(), object).toString());
			}
			
		}, choiceProvider, allowEmpty));
		branchChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target);
			}
			
		});
	}
	
	private String getBranchId() {
		return getModelObject();
	}

	private void setBranchId(String branchId) {
		getModel().setObject(branchId);
	}

	@Override
	protected void convertInput() {
		String branchId;
		String branch = branchChoice.getConvertedInput();
		if (branch != null) {
			branchId = new DepotAndBranch(selectedRepoModel.getObject(), branch).toString();
		} else {
			branchId = null;
		}
		
		setConvertedInput(branchId);
	}

	@Override
	protected void onDetach() {
		selectedRepoModel.detach();
		
		super.onDetach();
	}
	
}

package com.pmease.gitplex.web.component.branchchoice.affinalchoice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branchchoice.BranchSingleChoice;
import com.pmease.gitplex.web.component.repochoice.AffinalRepositoryChoice;

@SuppressWarnings("serial")
public class AffinalBranchSingleChoice extends FormComponentPanel<String> {

	private final IModel<Repository> currentRepoModel;
	
	private final IModel<Repository> selectedRepoModel;
	
	private final boolean allowEmpty;
	
	private BranchSingleChoice branchChoice;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param currentRepoModel
	 * 			model of current repository. Note that the model object should never be null
	 * @param selectedBranchModel
	 * 			model of selected branch
	 */
	public AffinalBranchSingleChoice(String id, IModel<Repository> currentRepoModel, 
			IModel<String> selectedBranchModel, boolean allowEmpty) {
		super(id, selectedBranchModel);
		
		this.currentRepoModel = currentRepoModel;
		
		selectedRepoModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				String branchId = getBranchId();
				if (branchId == null)
					return AffinalBranchSingleChoice.this.currentRepoModel.getObject();
				else 
					return new RepoAndBranch(branchId).getRepository();
			}

			@Override
			public void setObject(Repository object) {
				setBranchId(new RepoAndBranch(object, object.getDefaultBranch()).getId());
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
		
		add(new AffinalRepositoryChoice("repositoryChoice", currentRepoModel, selectedRepoModel).add(new AjaxFormComponentUpdatingBehavior("change") {
			
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
				return new RepoAndBranch(AffinalBranchSingleChoice.this.getModelObject()).getBranch();
			}

			@Override
			public void setObject(String object) {
				AffinalBranchSingleChoice.this.setModelObject(new RepoAndBranch(selectedRepoModel.getObject(), object).getId());
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
		setConvertedInput(branchChoice.getConvertedInput());
	}

	@Override
	protected void onDetach() {
		currentRepoModel.detach();
		selectedRepoModel.detach();
		
		super.onDetach();
	}
	
}

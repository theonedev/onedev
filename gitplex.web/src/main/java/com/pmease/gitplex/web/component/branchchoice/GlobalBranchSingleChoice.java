package com.pmease.gitplex.web.component.branchchoice;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repochoice.RepositoryChoice;

@SuppressWarnings("serial")
public class GlobalBranchSingleChoice extends FormComponentPanel<String> {

	private final IModel<Repository> repoModel;
	
	private final boolean allowEmpty;
	
	private BranchSingleChoice branchChoice;
	
	/**
	 * Construct with selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param branchModel
	 * 			model of selected branch
	 */
	public GlobalBranchSingleChoice(String id, IModel<String> branchModel, boolean allowEmpty) {
		super(id, branchModel);
		
		repoModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				String branchId = getBranchId();
				if (branchId != null) 
					return new RepoAndBranch(branchId).getRepository();
				else 
					return null;
			}

			@Override
			public void setObject(Repository object) {
				setBranchId(new RepoAndBranch(object, object.getDefaultBranch()).toString());
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
		
		add(new RepositoryChoice("repositoryChoice", repoModel, null, false).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchChoice);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(repoModel);
		add(branchChoice = new BranchSingleChoice("branchChoice", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return new RepoAndBranch(GlobalBranchSingleChoice.this.getModelObject()).getBranch();
			}

			@Override
			public void setObject(String object) {
				GlobalBranchSingleChoice.this.setModelObject(new RepoAndBranch(repoModel.getObject(), object).toString());
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
			branchId = new RepoAndBranch(repoModel.getObject(), branch).toString();
		} else {
			branchId = null;
		}
		
		setConvertedInput(branchId);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}

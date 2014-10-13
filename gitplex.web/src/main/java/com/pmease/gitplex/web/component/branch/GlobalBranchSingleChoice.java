package com.pmease.gitplex.web.component.branch;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repository.RepositoryChoice;

@SuppressWarnings("serial")
public class GlobalBranchSingleChoice extends FormComponentPanel<Branch> {

	private IModel<Repository> repositoryModel;
	
	private BranchSingleChoice branchChoice;
	
	/**
	 * Construct with selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param branchModel
	 * 			model of selected branch
	 */
	public GlobalBranchSingleChoice(String id, IModel<Branch> branchModel) {
		super(id, branchModel);
		
		repositoryModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				Branch branch = getBranch();
				if (branch != null) 
					return branch.getRepository();
				else 
					return null;
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
		
		add(new RepositoryChoice("repositoryChoice", repositoryModel, null).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchChoice);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(repositoryModel);
		add(branchChoice = new BranchSingleChoice("branchChoice", getModel(), choiceProvider));
		branchChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
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
		setConvertedInput(branchChoice.getConvertedInput());
	}

	@Override
	protected void onDetach() {
		repositoryModel.detach();
		
		super.onDetach();
	}
	
}

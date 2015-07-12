package com.pmease.gitplex.web.component.branch;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repochoice.RepositoryChoice;

@SuppressWarnings("serial")
public class GlobalBranchSingleChoice extends FormComponentPanel<RepoAndBranch> {

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
	public GlobalBranchSingleChoice(String id, IModel<RepoAndBranch> branchModel) {
		super(id, branchModel);
		
		repositoryModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				RepoAndBranch repoAndBranch = getRepoAndBranch();
				if (repoAndBranch != null) 
					return repoAndBranch.getRepository();
				else 
					return null;
			}

			@Override
			public void setObject(Repository object) {
				setRepoAndBranch(new RepoAndBranch(object, object.getDefaultBranch()));
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
	
	private RepoAndBranch getRepoAndBranch() {
		return getModelObject();
	}

	private void setRepoAndBranch(RepoAndBranch repoAndBranch) {
		getModel().setObject(repoAndBranch);
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

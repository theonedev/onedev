package com.pmease.gitplex.web.component.branch;

import java.util.Collection;
import java.util.HashSet;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repochoice.AffinalRepositoryChoice;

@SuppressWarnings("serial")
public class AffinalBranchMultiChoice extends FormComponentPanel<Collection<Branch>> {

	private IModel<Repository> currentRepositoryModel;
	
	private IModel<Repository> selectedRepositoryModel;
	
	private Long repoId;
	
	private BranchMultiChoice branchChoice;
	
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
				if (repoId != null) {
					return GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
				} else {
					Collection<Branch> branches = getBranches();
					if (branches == null || branches.isEmpty())
						return AffinalBranchMultiChoice.this.currentRepositoryModel.getObject();
					else 
						return branches.iterator().next().getRepository();
				}
			}

			@Override
			public void setObject(Repository object) {
				repoId = object.getId();
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
		
		add(new AffinalRepositoryChoice("repositoryChoice", currentRepositoryModel, selectedRepositoryModel).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchChoice);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(selectedRepositoryModel);
		
		add(branchChoice = new BranchMultiChoice("branchChoice", getModel(), choiceProvider));
		branchChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
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
		setConvertedInput(branchChoice.getConvertedInput());
	}

	@Override
	protected void onDetach() {
		currentRepositoryModel.detach();
		selectedRepositoryModel.detach();
		
		super.onDetach();
	}
	
}

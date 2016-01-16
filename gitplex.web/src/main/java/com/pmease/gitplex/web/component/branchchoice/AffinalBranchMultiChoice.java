package com.pmease.gitplex.web.component.branchchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.repochoice.AffinalRepositoryChoice;

@SuppressWarnings("serial")
public class AffinalBranchMultiChoice extends FormComponentPanel<Collection<String>> {

	private final Long currentRepoId;
	
	private IModel<Repository> selectedRepoModel;
	
	private Long selectedRepoId;
	
	private BranchMultiChoice branchChoice;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param currentRepoId
	 * 			id of current repository. Note that the model object should never be null
	 * @param selectedBranchesModel
	 * 			model of selected branch
	 */
	public AffinalBranchMultiChoice(String id, final Long currentRepoId, IModel<Collection<String>> selectedBranchesModel) {
		super(id, selectedBranchesModel);
		
		this.currentRepoId = currentRepoId;
		
		selectedRepoModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				Dao dao = GitPlex.getInstance(Dao.class);
				if (selectedRepoId != null) {
					return dao.load(Repository.class, selectedRepoId);
				} else {
					Collection<String> repoAndBranches = getRepoAndBranches();
					if (repoAndBranches == null || repoAndBranches.isEmpty())
						return dao.load(Repository.class, currentRepoId);
					else 
						return new RepoAndBranch(repoAndBranches.iterator().next()).getRepository();
				}
			}

			@Override
			public void setObject(Repository object) {
				selectedRepoId = object.getId();
				setRepoAndBranches(new HashSet<String>());
			}
			
		};
		
	}
	
	protected void onChange(AjaxRequestTarget target) {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new AffinalRepositoryChoice("repositoryChoice", currentRepoId, selectedRepoModel).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchChoice);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(selectedRepoModel);
		
		add(branchChoice = new BranchMultiChoice("branchChoice", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				Collection<String> branches = new ArrayList<>();
				for (String branchId: AffinalBranchMultiChoice.this.getModelObject())
					branches.add(new RepoAndBranch(branchId).getBranch());
				return branches;
			}

			@Override
			public void setObject(Collection<String> object) {
				Collection<String> branchIds = new ArrayList<>();
				for (String branch: object)
					branchIds.add(new RepoAndBranch(selectedRepoModel.getObject(), branch).toString());
				AffinalBranchMultiChoice.this.setModelObject(branchIds);
			}
			
		}, choiceProvider));
		
		branchChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target);
			}
			
		});
	}
	
	private Collection<String> getRepoAndBranches() {
		return getModelObject();
	}

	private void setRepoAndBranches(Collection<String> repoAndBranches) {
		getModel().setObject(repoAndBranches);
	}

	@Override
	protected void convertInput() {
		setConvertedInput(branchChoice.getConvertedInput());
	}

	@Override
	protected void onDetach() {
		selectedRepoModel.detach();
		
		super.onDetach();
	}
	
}

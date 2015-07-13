package com.pmease.gitplex.web.component.branchchoice.affinalchoice;

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
import com.pmease.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branchchoice.BranchMultiChoice;
import com.pmease.gitplex.web.component.repochoice.AffinalRepositoryChoice;

@SuppressWarnings("serial")
public class AffinalBranchMultiChoice extends FormComponentPanel<Collection<RepoAndBranch>> {

	private IModel<Repository> currentRepoModel;
	
	private IModel<Repository> selectedRepoModel;
	
	private Long repoId;
	
	private BranchMultiChoice branchChoice;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param currentRepoModel
	 * 			model of current repository. Note that the model object should never be null
	 * @param selectedBranchesModel
	 * 			model of selected branch
	 */
	public AffinalBranchMultiChoice(String id, IModel<Repository> currentRepoModel, 
			IModel<Collection<RepoAndBranch>> selectedBranchesModel) {
		super(id, selectedBranchesModel);
		
		this.currentRepoModel = currentRepoModel;
		
		selectedRepoModel = new IModel<Repository>() {

			@Override
			public void detach() {
			}

			@Override
			public Repository getObject() {
				if (repoId != null) {
					return GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
				} else {
					Collection<RepoAndBranch> repoAndBranches = getRepoAndBranches();
					if (repoAndBranches == null || repoAndBranches.isEmpty())
						return AffinalBranchMultiChoice.this.currentRepoModel.getObject();
					else 
						return repoAndBranches.iterator().next().getRepository();
				}
			}

			@Override
			public void setObject(Repository object) {
				repoId = object.getId();
				setRepoAndBranches(new HashSet<RepoAndBranch>());
			}
			
		};
		
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
		
		add(branchChoice = new BranchMultiChoice("branchChoice", getModel(), choiceProvider));
		branchChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target);
			}
			
		});
	}
	
	private Collection<RepoAndBranch> getRepoAndBranches() {
		return getModelObject();
	}

	private void setRepoAndBranches(Collection<RepoAndBranch> repoAndBranches) {
		getModel().setObject(repoAndBranches);
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

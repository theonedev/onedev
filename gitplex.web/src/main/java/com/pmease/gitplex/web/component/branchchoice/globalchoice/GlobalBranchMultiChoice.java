package com.pmease.gitplex.web.component.branchchoice.globalchoice;

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
import com.pmease.gitplex.web.component.repochoice.RepositoryChoice;

@SuppressWarnings("serial")
public class GlobalBranchMultiChoice extends FormComponentPanel<Collection<RepoAndBranch>> {

	private Long repoId;
	
	private IModel<Repository> repositoryModel;
	
	private BranchMultiChoice branchChoice;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param branchesModel
	 * 			model of selected branches
	 */
	public GlobalBranchMultiChoice(String id, IModel<Collection<RepoAndBranch>> repoAndBranchesModel) {
		super(id, repoAndBranchesModel);
		
		repositoryModel = new IModel<Repository>() {

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
						return null;
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
		
		add(new RepositoryChoice("repositoryChoice", repositoryModel, null).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchChoice);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(repositoryModel);
		
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
		repositoryModel.detach();
		
		super.onDetach();
	}
	
}

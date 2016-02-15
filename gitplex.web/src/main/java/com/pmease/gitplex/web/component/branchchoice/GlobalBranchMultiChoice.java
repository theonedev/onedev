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
import com.pmease.gitplex.core.model.DepotAndBranch;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.repochoice.DepotChoice;

@SuppressWarnings("serial")
public class GlobalBranchMultiChoice extends FormComponentPanel<Collection<String>> {

	private Long repoId;
	
	private IModel<Depot> depotModel;
	
	private BranchMultiChoice branchChoice;
	
	/**
	 * Construct with current repository model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param branchesModel
	 * 			model of selected branches
	 */
	public GlobalBranchMultiChoice(String id, IModel<Collection<String>> branchIdsModel) {
		super(id, branchIdsModel);
		
		depotModel = new IModel<Depot>() {

			@Override
			public void detach() {
			}

			@Override
			public Depot getObject() {
				if (repoId != null) {
					return GitPlex.getInstance(Dao.class).load(Depot.class, repoId);
				} else {
					Collection<String> branchIds = getBranchIds();
					if (branchIds == null || branchIds.isEmpty())
						return null;
					else 
						return new DepotAndBranch(branchIds.iterator().next()).getDepot();
				}
			}

			@Override
			public void setObject(Depot object) {
				repoId = object.getId();
				setBranchIds(new HashSet<String>());
			}
			
		};
		
	}
	
	protected void onChange(AjaxRequestTarget target) {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new DepotChoice("repositoryChoice", depotModel, null, false).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchChoice);
				onChange(target);
			}

		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(depotModel);
		
		add(branchChoice = new BranchMultiChoice("branchChoice", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				Collection<String> branches = new ArrayList<>();
				for (String branchId: GlobalBranchMultiChoice.this.getModelObject())
					branches.add(new DepotAndBranch(branchId).getBranch());
				return branches;
			}

			@Override
			public void setObject(Collection<String> object) {
				Collection<String> branchIds = new ArrayList<>();
				for (String branch: object)
					branchIds.add(new DepotAndBranch(depotModel.getObject(), branch).toString());
				GlobalBranchMultiChoice.this.setModelObject(branchIds);
			}
			
		}, choiceProvider));
		
		branchChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target);
			}
			
		});
	}
	
	private Collection<String> getBranchIds() {
		return getModelObject();
	}

	private void setBranchIds(Collection<String> branchIds) {
		getModel().setObject(branchIds);
	}

	@Override
	protected void convertInput() {
		Collection<String> branchIds = new ArrayList<>();
		Collection<String> branches = branchChoice.getConvertedInput();
		if (branches != null) {
			for (String branch: branches)
				branchIds.add(new DepotAndBranch(depotModel.getObject(), branch).toString());
		}
		
		setConvertedInput(branchIds);
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}
	
}

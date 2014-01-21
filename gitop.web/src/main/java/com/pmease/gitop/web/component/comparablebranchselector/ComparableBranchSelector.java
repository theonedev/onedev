package com.pmease.gitop.web.component.comparablebranchselector;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.component.choice.BranchChoiceProvider;
import com.pmease.gitop.web.component.choice.BranchSingleChoice;
import com.pmease.gitop.web.component.choice.ComparableProjectChoice;

@SuppressWarnings("serial")
public class ComparableBranchSelector extends FormComponentPanel<Branch> {

	private IModel<Project> currentProjectModel;
	
	private IModel<Project> selectedProjectModel;
	
	private final String projectLabel;
	
	private final String branchLabel;
	
	/**
	 * Construct with current project model and selected branch model.
	 * 
	 * @param id
	 * 			id of the component
	 * @param currentProjectModel
	 * 			model of current project. Note that the model object should never be null
	 * @param selectedBranchModel
	 * 			model of selected branch
	 * @param proejctLabel
	 * 			label of the project choice
	 * @param branchLabel
	 * 			label of the branch choice
	 */
	public ComparableBranchSelector(String id, IModel<Project> currentProjectModel, IModel<Branch> selectedBranchModel, 
			@Nullable String projectLabel, @Nullable String branchLabel) {
		super(id, selectedBranchModel);
		
		this.currentProjectModel = currentProjectModel;
		
		selectedProjectModel = new IModel<Project>() {

			@Override
			public void detach() {
			}

			@Override
			public Project getObject() {
				Branch branch = getBranch();
				if (branch == null) {
					return ComparableBranchSelector.this.currentProjectModel.getObject();
				} else {
					return branch.getProject();
				}
			}

			@Override
			public void setObject(Project object) {
				setBranch(Gitop.getInstance(BranchManager.class).findDefault(object));
			}
			
		};
		
		this.projectLabel = projectLabel;
		this.branchLabel = branchLabel;
	}
	
	protected void onChange(AjaxRequestTarget target) {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new Label("projectLabel", (projectLabel != null? projectLabel: "Project")));
		add(new Label("branchLabel", (branchLabel != null? branchLabel: "Branch")));

		add(new ComparableProjectChoice("projectChoice", currentProjectModel, selectedProjectModel).add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(ComparableBranchSelector.this);
				onChange(target);
			}
			
		}));
		
		BranchChoiceProvider choiceProvider = new BranchChoiceProvider(new LoadableDetachableModel<DetachedCriteria>() {

			@Override
			protected DetachedCriteria load() {
				DetachedCriteria criteria = DetachedCriteria.forClass(Branch.class);
				criteria.add(Restrictions.eq("project", selectedProjectModel.getObject()));
				return criteria;
			}
			
		});
		add(new BranchSingleChoice("branchChoice", getModel(), choiceProvider).add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onChange(target);
			}
			
		}));
	}
	
	private Branch getBranch() {
		return getModelObject();
	}

	private void setBranch(Branch branch) {
		getModel().setObject(branch);
	}

	@Override
	protected void onDetach() {
		currentProjectModel.detach();
		selectedProjectModel.detach();
		
		super.onDetach();
	}
	
}

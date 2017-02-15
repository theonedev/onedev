package com.gitplex.server.web.component.branchpicker;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.web.component.DepotPicker;
import com.gitplex.server.web.util.model.AffinalDepotsModel;

@SuppressWarnings("serial")
public abstract class AffinalBranchPicker extends Panel {

	private Long depotId;
	
	private String branch;
	
	public AffinalBranchPicker(String id, Long repoId, String branch) {
		super(id);
		
		this.depotId = repoId;
		this.branch = branch;
	}
	
	private void newBranchPicker(@Nullable AjaxRequestTarget target) {
		BranchPicker branchPicker = new BranchPicker("branchPicker", new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				return getDepot();
			}
			
		}, branch) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String branch) {
				AffinalBranchPicker.this.onSelect(target, getDepot(), branch);
			}

		};
		if (target != null) {
			replace(branchPicker);
			target.add(branchPicker);
		} else {
			add(branchPicker);
		}
	}
	
	private Depot getDepot() {
		return GitPlex.getInstance(Dao.class).load(Depot.class, depotId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DepotPicker("repositoryPicker", new AffinalDepotsModel(depotId), depotId) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot) {
				depotId = depot.getId();
				branch = depot.getDefaultBranch();
				newBranchPicker(target);
				AffinalBranchPicker.this.onSelect(target, depot, branch);
			}
			
		});
		newBranchPicker(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BranchPickerResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, Depot depot, String branch);
	
}

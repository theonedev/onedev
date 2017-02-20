package com.gitplex.server.web.component.revisionpicker;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.Depot;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.web.component.DepotPicker;
import com.gitplex.server.web.util.model.AffinalDepotsModel;

@SuppressWarnings("serial")
public abstract class AffinalRevisionPicker extends Panel {

	private Long depotId;
	
	private String revision;
	
	public AffinalRevisionPicker(String id, Long repoId, String revision) {
		super(id);
		
		this.depotId = repoId;
		this.revision = revision;
	}
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		RevisionPicker revisionPicker = new RevisionPicker("revisionPicker", new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				return getDepot();
			}
			
		}, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				AffinalRevisionPicker.this.onSelect(target, getDepot(), revision);
			}

		};
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
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
				revision = depot.getDefaultBranch();
				newRevisionPicker(target);
				AffinalRevisionPicker.this.onSelect(target, depot, revision);
			}
			
		});
		newRevisionPicker(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RevisionPickerResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, Depot depot, String revision);
	
}

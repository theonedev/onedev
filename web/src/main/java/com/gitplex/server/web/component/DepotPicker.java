package com.gitplex.server.web.component;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.gitplex.server.GitPlex;
import com.gitplex.server.model.Depot;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.web.component.depotselector.DepotSelector;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class DepotPicker extends DropdownLink {

	private final IModel<Collection<Depot>> depotsModel; 
	
	private Long currentDepotId;
	
	public DepotPicker(String id, IModel<Collection<Depot>> depotsModel, Long currentDepotId) {
		super(id);
	
		this.depotsModel = depotsModel;
		this.currentDepotId = currentDepotId;
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new DepotSelector(id, depotsModel, currentDepotId) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot) {
				dropdown.close();
				target.add(DepotPicker.this);
				DepotPicker.this.onSelect(target, depot);
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setEscapeModelStrings(false);
	}

	@Override
	public IModel<?> getBody() {
		Depot currentDepot = GitPlex.getInstance(Dao.class).load(Depot.class, currentDepotId);
		return Model.of(String.format("<i class='fa fa-ext fa-repo'></i> <span>%s</span> <i class='fa fa-caret-down'></i>", currentDepot.getFQN()));
	}

	@Override
	protected void onDetach() {
		depotsModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, Depot depot);
}

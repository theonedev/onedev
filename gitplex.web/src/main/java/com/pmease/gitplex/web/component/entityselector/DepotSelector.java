package com.pmease.gitplex.web.component.entityselector;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public abstract class DepotSelector extends EntitySelector<Depot> {

	public DepotSelector(String id, IModel<Collection<Depot>> depotsModel, Long currentDepotId) {
		super(id, depotsModel, currentDepotId);
	}

	@Override
	protected String getUrl(Depot entity) {
		PageParameters params = DepotFilePage.paramsOf(entity);
		return urlFor(DepotFilePage.class, params).toString();
	}

	@Override
	protected String getNotFoundMessage() {
		return "No repositories found";
	}

	@Override
	protected Component renderEntity(String componentId, IModel<Depot> entityModel) {
		Depot depot = entityModel.getObject();
		String label = depot.getAccount().getName() + " " + Depot.FQN_SEPARATOR + " " + depot.getName();
		return new Label(componentId, "<i class='fa fa-ext fa-repo'></i> " + label).setEscapeModelStrings(false);
	}

	@Override
	protected boolean matches(Depot entity, String searchTerm) {
		return entity.matchesFQN(searchTerm);
	}

}

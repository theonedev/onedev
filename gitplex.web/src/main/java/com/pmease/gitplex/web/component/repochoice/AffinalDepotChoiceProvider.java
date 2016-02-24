package com.pmease.gitplex.web.component.repochoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.component.select2.ListChoiceProvider;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.model.AffinalDepotsModel;

@SuppressWarnings("serial")
public class AffinalDepotChoiceProvider extends ListChoiceProvider<Depot> {

	private final Long depotId;
	
	private IModel<List<Depot>> affinalDepotsModel;

	public AffinalDepotChoiceProvider(Long repoId) {
		super(Constants.DEFAULT_PAGE_SIZE);
	
		this.depotId = repoId;
		affinalDepotsModel = new AffinalDepotsModel(repoId);
	}
	
	@Override
	public void toJson(Depot choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		String value;
		if (getDepot().equals(choice))
			value = "<i>Current</i>";
		else if (choice.equals(getDepot().getForkedFrom()))
			value = "<i>UpStream</i>";
		else
			value = StringEscapeUtils.escapeHtml4(choice.getOwner().getName() + "/" + choice.getName());
		writer.value(value);
	}

	@Override
	public Collection<Depot> toChoices(Collection<String> ids) {
		List<Depot> repositories = Lists.newArrayList();
		Dao dao = GitPlex.getInstance(Dao.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			repositories.add(dao.load(Depot.class, id));
		}

		return repositories;
	}

	private Depot getDepot() {
		return GitPlex.getInstance(Dao.class).load(Depot.class, depotId);
	}
	
	private List<Depot> getAffinalDepots() {
		return affinalDepotsModel.getObject();
	}

	@Override
	public void detach() {
		affinalDepotsModel.detach();
		
		super.detach();
	}

	@Override
	protected List<Depot> filterList(String term) {
		term = term.toLowerCase();
		List<Depot> depots = new ArrayList<>();
		for (Depot depot: getAffinalDepots()) {
			if (depot.getOwner().getName().toLowerCase().startsWith(term))
				depots.add(depot);
		}
		return depots;
	}
	
}
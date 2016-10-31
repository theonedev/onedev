package com.gitplex.web.component.depotchoice;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.manager.DepotManager;
import com.google.common.collect.Lists;
import com.gitplex.commons.wicket.component.select2.ChoiceProvider;

@SuppressWarnings("serial")
public abstract class AbstractDepotChoiceProvider extends ChoiceProvider<Depot> {

	@Override
	public void toJson(Depot choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		writer.value(StringEscapeUtils.escapeHtml4(choice.getName()));
	}
	
	@Override
	public Collection<Depot> toChoices(Collection<String> ids) {
		List<Depot> list = Lists.newArrayList();
		DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(depotManager.load(id));
		}
		
		return list;
	}

}

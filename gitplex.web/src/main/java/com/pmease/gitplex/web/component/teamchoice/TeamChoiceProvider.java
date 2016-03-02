package com.pmease.gitplex.web.component.teamchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.pmease.gitplex.core.entity.Account;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class TeamChoiceProvider extends ChoiceProvider<String> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 10;
	
	private final IModel<Account> organizationModel;
		
	public TeamChoiceProvider(IModel<Account> organizationModel) {
		this.organizationModel = organizationModel;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		term = term.toLowerCase();
		Account organization = organizationModel.getObject();
		List<String> matchedTeams = new ArrayList<>();
		for (String teamName: organization.getTeams().keySet()) {
			if (teamName.toLowerCase().contains(term)) {
				matchedTeams.add(teamName);
			}
		}
		int from = page*PAGE_SIZE;
		int to = from + PAGE_SIZE;
		if (to > matchedTeams.size())
			to = matchedTeams.size();
		if (from > to)
			from = to;
		response.addAll(matchedTeams.subList(page*PAGE_SIZE, (page+1)*PAGE_SIZE));
	}

	@Override
	public void toJson(String choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice).key("name").value(choice);
	}

	@Override
	public Collection<String> toChoices(Collection<String> ids) {
		return ids;
	}

	@Override
	public void detach() {
		super.detach();

		organizationModel.detach();
	}
}
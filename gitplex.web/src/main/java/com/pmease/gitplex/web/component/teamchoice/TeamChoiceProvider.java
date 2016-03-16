package com.pmease.gitplex.web.component.teamchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.Constants;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

public class TeamChoiceProvider extends ChoiceProvider<String> {

	private static final long serialVersionUID = 1L;

	private final IModel<Account> organizationModel;
		
	public TeamChoiceProvider(IModel<Account> organizationModel) {
		this.organizationModel = organizationModel;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		term = term.toLowerCase();
		Account organization = organizationModel.getObject();
		List<String> teams = new ArrayList<>();
		for (String teamName: organization.getTeams().keySet()) {
			if (teamName.toLowerCase().contains(term)) {
				teams.add(teamName);
			}
		}
		
		new ResponseFiller<String>(response).fill(teams, page, Constants.DEFAULT_PAGE_SIZE);
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
package com.gitplex.server.web.page.depot.setting.tagprotection;

import javax.validation.constraints.NotNull;

import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.model.support.tagcreator.TagCreator;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable
public class TeamlessTagProtection extends TagProtection {

	private static final long serialVersionUID = 1L;

	private TeamlessTagCreator teamlessTagCreator = new TeamlessDepotWriters();
	
	@Override
	public TagCreator getTagCreator() {
		return super.getTagCreator();
	}

	@Editable(order=400, name="Allowed Creator", description="Specify users allowed to create specified tag")
	@NotNull
	public TeamlessTagCreator getTeamlessTagCreator() {
		return teamlessTagCreator;
	}

	public void setTeamlessTagCreator(TeamlessTagCreator teamlessTagCreator) {
		this.teamlessTagCreator = teamlessTagCreator;
	}

}

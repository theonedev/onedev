package io.onedev.server.util.inputspec.userchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.TeamChoice;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=150, name="Users belonging to team")
public class TeamUsers implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	private String teamName;

	@Editable(name="Team")
	@TeamChoice
	@OmitName
	@NotEmpty
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Override
	public List<UserFacade> getChoices(boolean allPossible) {
		List<UserFacade> choices = new ArrayList<>();
		Team team = OneDev.getInstance(TeamManager.class).find(OneContext.get().getProject(), teamName);
		if (team != null) {
			for (User user: team.getMembers())
				choices.add(user.getFacade());
		}
		return choices;
	}

}

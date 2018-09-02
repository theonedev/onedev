package io.onedev.server.model.support.ifsubmittedby;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.TeamChoice;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=300, name="Team")
public class SpecifiedTeam implements IfSubmittedBy {

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
	public boolean matches(Project project, User user) {
		TeamManager teamManager = OneDev.getInstance(TeamManager.class);
		Team team = Preconditions.checkNotNull(teamManager.find(project, teamName));
		return team.getMembers().contains(user);
	}

}

package com.pmease.gitop.core.gatekeeper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitNames;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.DirectoryChoice;
import com.pmease.gitop.core.editable.TeamChoice;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.gatekeeper.AndGateKeeper;
import com.pmease.gitop.model.gatekeeper.CommonGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.IfThenGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=200, icon="icon-lock", description="By default, users with write permission of "
		+ "the project can write to all directories. Use this gate keeper to restrict write "
		+ "access of certain directories to certain users.")
@OmitNames
public class DirectoryProtection extends CommonGateKeeper {
	
	private List<Entry> entries = new ArrayList<Entry>();
	
	public DirectoryProtection() {
		Entry entry = new Entry();
		entries.add(entry);
	}
	
	@Editable(name="Protected Directories", order=100)
	@Valid
	@Size(min=1)
	@NotNull
	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	@Editable
	public static class Entry implements Trimmable, Serializable {
		
		private String directory;
		
		private Long teamId;

		@Editable(name="Directory to Protect", order=100)
		@DirectoryChoice
		@NotEmpty
		public String getDirectory() {
			return directory;
		}

		public void setDirectory(String directory) {
			this.directory = directory;
		}

		@Editable(name="Team Can Write", order=200)
		@TeamChoice(excludes={Team.ANONYMOUS, Team.LOGGEDIN})
		@NotNull
		public Long getTeamId() {
			return teamId;
		}

		public void setTeamId(Long teamId) {
			this.teamId = teamId;
		}

		@Override
		public Object trim(Object context) {
			if (Gitop.getInstance(TeamManager.class).get(teamId) == null)
				return null;
			else
				return this;
		}

	}

	@Override
	public CheckResult doCheck(PullRequest request) {
		AndGateKeeper andGateKeeper = new AndGateKeeper();
		for (Entry entry: entries) {
			IfThenGateKeeper ifThenGateKeeper = new IfThenGateKeeper();
			IfTouchesSpecifiedDirectory ifGate = new IfTouchesSpecifiedDirectory();
			ifGate.setDirectories(entry.getDirectory());
			ifThenGateKeeper.setIfGate(ifGate);
			
			IfApprovedBySpecifiedTeam thenGate = new IfApprovedBySpecifiedTeam();
			thenGate.setTeamId(entry.getTeamId());
			ifThenGateKeeper.setThenGate(thenGate);
			
			andGateKeeper.getGateKeepers().add(ifThenGateKeeper);
		}
		return andGateKeeper.doCheck(request);
	}

	@Override
	protected GateKeeper trim(Project project) {
		TrimUtils.trim(entries, project);

		if (entries.isEmpty())
			return null;
		else
			return this;
	}

}

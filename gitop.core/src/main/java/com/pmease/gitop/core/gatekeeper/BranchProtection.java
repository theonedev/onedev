package com.pmease.gitop.core.gatekeeper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.BranchChoice;
import com.pmease.gitop.core.editable.TeamChoice;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.AndGateKeeper;
import com.pmease.gitop.model.gatekeeper.CommonGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.IfThenGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=100, icon="icon-lock", description="By default, users with write permission of "
		+ "the project can push code to all branches. Use this gate keeper to restrict write "
		+ "access of certain branches to certain teams.")
public class BranchProtection extends CommonGateKeeper {
	
	private List<Entry> entries = new ArrayList<Entry>();
	
	public BranchProtection() {
		Entry entry = new Entry();
		entries.add(entry);
	}
	
	@Editable
	@Size(min=1, message="At least one entry has to be specified.")
	@Valid
	@NotNull
	@OmitName
	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	@Editable
	public static class Entry implements Trimmable, Serializable {
		
		private Long branchId;
		
		private Long teamId;

		@Editable(name="Branch To Protect", order=100)
		@BranchChoice
		@NotNull
		public Long getBranchId() {
			return branchId;
		}

		public void setBranchId(Long branchId) {
			this.branchId = branchId;
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
			if (Gitop.getInstance(BranchManager.class).get(branchId) == null)
				return null;
			if (Gitop.getInstance(TeamManager.class).get(teamId) == null)
				return null;
			return this;
		}

	}

	@Override
	protected GateKeeper trim(Project project) {
		TrimUtils.trim(entries, project);

		if (entries.isEmpty())
			return null;
		else
			return this;
	}

	private GateKeeper getGateKeeper() {
		AndGateKeeper andGate = new AndGateKeeper();
		for (Entry entry: entries) {
			IfThenGateKeeper ifThenGate = new IfThenGateKeeper();
			IfSubmittedToSpecifiedBranches ifGate = new IfSubmittedToSpecifiedBranches();
			ifGate.getBranchIds().add(entry.getBranchId());
			ifThenGate.setIfGate(ifGate);
			
			IfApprovedBySpecifiedTeam thenGate = new IfApprovedBySpecifiedTeam();
			thenGate.setTeamId(entry.getTeamId());
			ifThenGate.setThenGate(thenGate);
			
			andGate.getGateKeepers().add(ifThenGate);
		}
		return andGate;
	}

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		return getGateKeeper().checkRequest(request);
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, @Nullable String file) {
		return getGateKeeper().checkFile(user, branch, file);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return getGateKeeper().checkCommit(user, branch, commit);
	}

}

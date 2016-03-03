package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.Min;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;

@Editable(order=300, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit gets specified number "
		+ "of disapprovals from specified team. It normally works together "
		+ "with a NOT container to reject the pull request in case there are "
		+ "disapprovals")
public class IfDisapprovedBySpecifiedTeam extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private String teamName;
	
	private int leastDisapprovals = 1;

    @Editable(name="Team", order=100)
    @NotEmpty
    public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Editable(name="Least Disapprovals Required", order=200)
    @Min(value = 1, message = "Least disapprovals should not be less than 1")
	public int getLeastDisapprovals() {
		return leastDisapprovals;
	}

	public void setLeastDisapprovals(int leastDisapprovals) {
		this.leastDisapprovals = leastDisapprovals;
	}

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
        int disapprovals = 0;
        
    	Collection<Account> members = new ArrayList<>();
    	for (Membership membership: request.getTargetDepot().getOwner().getUserMemberships()) {
    		if (membership.getJoinedTeams().contains(teamName))
    			members.add(membership.getUser());
    	}
        
        for (Account member : members) {
            Review.Result result = member.checkReviewSince(request.getReferentialUpdate());
            if (result == Review.Result.DISAPPROVE) {
                disapprovals++;
            }
        }

        if (disapprovals >= getLeastDisapprovals()) {
            return passed(Lists.newArrayList("Disapproved by at least " + getLeastDisapprovals() 
            		+ " member(s) of team " + teamName));
        } else {
            return failed(Lists.newArrayList("Not disapproved by at least " + getLeastDisapprovals() 
            		+ " member(s) of team " + teamName));
        }
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return failed(Lists.newArrayList("Not disapproved by anyone from team " + teamName));
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit,
			ObjectId newCommit) {
		return failed(Lists.newArrayList("Not disapproved by anyone from team " + teamName));
	}
	
	@Override
	public void onTeamRename(String oldName, String newName) {
		if (teamName.equals(oldName))
			teamName = newName;
	}

	@Override
	public boolean onTeamDelete(String teamName) {
		return this.teamName.equals(teamName);
	}

	@Override
	public boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, 
			Account originalOwner) {
		return depotDefiningGateKeeper.equals(transferredDepot);
	}
	
}

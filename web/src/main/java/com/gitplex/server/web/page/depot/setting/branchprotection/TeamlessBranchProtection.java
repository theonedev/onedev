package com.gitplex.server.web.page.depot.setting.branchprotection;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.FileProtection;
import com.gitplex.server.util.editable.annotation.AccountChoice;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable
public class TeamlessBranchProtection extends BranchProtection {

	private static final long serialVersionUID = 1L;

	private List<String> reviewerNames;
	
	private List<TeamlessFileProtection> teamlessFileProtections = new ArrayList<>();
	
	@Override
	public String getReviewAppointmentExpr() {
		return super.getReviewAppointmentExpr();
	}

	@Override
	public List<FileProtection> getFileProtections() {
		return super.getFileProtections();
	}

	@Editable(order=400, name="Reviewers", description="Optionally specify required reviewers for changes of "
			+ "specified branch. Note that the user submitting the change is considered to reviewed the change "
			+ "automatically")
	@AccountChoice(type=AccountChoice.Type.DEPOT_READER)
	public List<String> getReviewerNames() {
		return reviewerNames;
	}

	public void setReviewerNames(List<String> reviewerNames) {
		this.reviewerNames = reviewerNames;
	}

	@Editable(order=500, name="File Protections", description="Optionally specify additional users to review particular paths. For each changed file, "
			+ "the first matched file protection setting will be used")
	@NotNull
	public List<TeamlessFileProtection> getTeamlessFileProtections() {
		return teamlessFileProtections;
	}

	public void setTeamlessFileProtections(List<TeamlessFileProtection> teamlessFileProtections) {
		this.teamlessFileProtections = teamlessFileProtections;
	}

}

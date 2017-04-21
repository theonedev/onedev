package com.gitplex.server.web.page.depot.setting.branchprotection;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.model.support.FileProtection;
import com.gitplex.server.util.editable.annotation.AccountChoice;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable
public class TeamlessFileProtection extends FileProtection {

	private static final long serialVersionUID = 1L;
	
	private List<String> reviewerNames;

	public String getReviewAppointmentExpr() {
		return super.getReviewAppointmentExpr();
	}

	@Editable(order=200, name="Reviewers", description="Optionally specify required reviewers if specified path is "
			+ "changed. Note that the user submitting the change is considered to reviewed the change automatically")
	@AccountChoice(type=AccountChoice.Type.DEPOT_READER)
	@NotEmpty
	public List<String> getReviewerNames() {
		return reviewerNames;
	}

	public void setReviewerNames(List<String> reviewerNames) {
		this.reviewerNames = reviewerNames;
	}
	
}

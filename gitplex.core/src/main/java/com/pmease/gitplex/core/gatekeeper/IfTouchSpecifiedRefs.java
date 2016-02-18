package com.pmease.gitplex.core.gatekeeper;

import javax.validation.ConstraintValidatorContext;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.validation.ClassValidating;
import com.pmease.commons.validation.Validatable;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.util.editable.RefMatch;
import com.pmease.gitplex.core.util.refmatch.RefMatchUtils;

@SuppressWarnings("serial")
@ClassValidating
@Editable(order=100, icon="fa-ext fa-branch", category=GateKeeper.CATEGORY_CHECK_REFS, description=
		"This gate keeper will be passed if specified refs is being changed with specified operations.")
public class IfTouchSpecifiedRefs extends AbstractGateKeeper implements Validatable {

	private String refMatch;
	
	private boolean onCreate;
	
	private boolean onUpdate;
	
	private boolean onDelete;
	
	@Editable(name="Refs", order=100, description="Specify refs to match")
	@RefMatch
	@NotEmpty
	public String getRefMatch() {
		return refMatch;
	}

	public void setRefMatch(String refMatch) {
		this.refMatch = refMatch;
	}

	@Editable(name="On Create", order=200, description="If checked, this gate keeper will "
			+ "be passed if someone tries to create specified refs.")
	public boolean isOnCreate() {
		return onCreate;
	}

	public void setOnCreate(boolean onCreate) {
		this.onCreate = onCreate;
	}

	@Editable(name="On Update", order=300, description="If checked, this gate keeper will "
			+ "be passed if someone tries to update specified refs.")
	public boolean isOnUpdate() {
		return onUpdate;
	}

	public void setOnUpdate(boolean onUpdate) {
		this.onUpdate = onUpdate;
	}

	@Editable(name="On Delete", order=400, description="If checked, this gate keeper will "
			+ "be passed if someone tries to delete specified refs.")
	public boolean isOnDelete() {
		return onDelete;
	}

	public void setOnDelete(boolean onDelete) {
		this.onDelete = onDelete;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		if (RefMatchUtils.matches(refMatch, request.getTargetRef())) {
			if (onUpdate)
				return passed(Lists.newArrayList("Ref being updated matches '" + refMatch + "'."));
			else
				return failed(Lists.newArrayList("Ref being updated matches '" + refMatch + "', but operation is not 'update'."));
		} else {
			return failed(Lists.newArrayList("Ref does not match '" + refMatch + "'."));
		}
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		if (RefMatchUtils.matches(refMatch, GitUtils.branch2ref(branch))) {
			if (onUpdate)
				return passed(Lists.newArrayList("Ref being updated matches '" + refMatch + "'."));
			else
				return failed(Lists.newArrayList("Ref matches '" + refMatch + "', but operation is not 'update'."));
		} else {
			return failed(Lists.newArrayList("Target ref does not match '" + refMatch + "'."));
		}
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (RefMatchUtils.matches(refMatch, refName)) {
			if (oldCommit.equals(ObjectId.zeroId())) {
				if (onCreate)
					return passed(Lists.newArrayList("Ref being created matches '" + refMatch + "'."));
				else
					return failed(Lists.newArrayList("Ref matches '" + refMatch + "', but operation is not 'create'."));
			} else if (newCommit.equals(ObjectId.zeroId())) {
				if (onDelete)
					return passed(Lists.newArrayList("Ref being deleted matches '" + refMatch + "'."));
				else
					return failed(Lists.newArrayList("Ref matches '" + refMatch + "', but operation is not 'delete'."));
			} else {
				if (onUpdate)
					return passed(Lists.newArrayList("Ref being updated matches '" + refMatch + "'."));
				else
					return failed(Lists.newArrayList("Ref matches '" + refMatch + "', but operation is not 'update'."));
			}
		} else {
			return failed(Lists.newArrayList("Ref does not match '" + refMatch + "'."));
		}
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (!onCreate && !onUpdate && !onDelete) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Please select at least one operation to check").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
